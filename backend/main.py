from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect
from mangum import Mangum
from pydantic import BaseModel
from langchain_core.messages import HumanMessage
import dotenv
dotenv.load_dotenv()
from agents.orchestrator import orchestrator_app
from agents import process_vision_request
from agents.ops import update_inventory_database
from agents.devops import generate_and_deploy_pwa
from database import get_db, engine, Base
from models.inventory import Item
from sqlalchemy.orm import Session
from fastapi import Depends

# Initialize Database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="NBN CTO Agent API",
    description="Serverless API for the NBN agentic system",
    version="1.0.0"
)

# Request Models
class MessageRequest(BaseModel):
    user_id: str
    message: str

class VisionRequest(BaseModel):
    user_id: str
    image_url: str

class DeployRequest(BaseModel):
    campaign_name: str
    headline: str
    product_ids: str

class InventoryAddRequest(BaseModel):
    name: str
    quantity: int
    price: float
    description: str
    image_url: str = ""

@app.get("/")
def read_root():
    return {"status": "healthy", "service": "NBN Agent API"}

@app.post("/orchestrator/chat")
def orchestrator_chat(request: MessageRequest):
    # Initialize LangGraph Orchestrator
    initial_state = {
        "messages": [HumanMessage(content=request.message)],
        "user_id": request.user_id,
        "next_node": "",
        "actions": []
    }
    
    final_state = orchestrator_app.invoke(initial_state)
    
    # Extract AI reply from the final message
    reply = "I've processed your request."
    if final_state["messages"]:
        last_message = final_state["messages"][-1]
        reply = last_message.content
        
    return {
        "reply": reply,
        "actions": final_state.get("actions", [])
    }

# WebSocket endpoint for real-time agent StateFlow updates
@app.websocket("/ws/{client_id}")
async def websocket_endpoint(websocket: WebSocket, client_id: str):
    await websocket.accept()
    try:
        while True:
            data = await websocket.receive_text()
            # We can stream intermediate LangGraph states here
            await websocket.send_text(f"Message text was: {data}")
    except WebSocketDisconnect:
        print(f"Client {client_id} disconnected")

@app.post("/ops/vision")
def ops_vision(request: VisionRequest):
    # Trigger Gemini Multimodal vision parsing
    extracted_data = process_vision_request(request.image_url)
    
    if extracted_data["status"] == "success":
        # Update PostgreSQL inventory
        update_inventory_database.invoke({
            "name": extracted_data["detected_item"],
            "quantity": extracted_data["quantity"],
            "price": extracted_data["estimated_price"],
            "description": "Auto-scanned via Gemini Vision"
        })
        
    return extracted_data

@app.post("/devops/deploy")
def devops_deploy(request: DeployRequest):
    # Trigger PWA deployment
    result = generate_and_deploy_pwa.invoke({
        "campaign_name": request.campaign_name,
        "headline": request.headline,
        "product_ids": request.product_ids
    })
    return {"status": "deployment_queued", "details": result}

@app.get("/inventory")
def get_inventory(db: Session = Depends(get_db)):
    items = db.query(Item).order_by(Item.updated_at.desc()).all()
    return items

@app.post("/inventory")
def add_inventory(request: InventoryAddRequest, db: Session = Depends(get_db)):
    new_item = Item(
        name=request.name,
        quantity=request.quantity,
        price=request.price,
        description=request.description,
        image_url=request.image_url,
        show_on_website=True
    )
    db.add(new_item)
    db.commit()
    db.refresh(new_item)
    return new_item

# Mangum adapter for AWS Lambda
handler = Mangum(app)
