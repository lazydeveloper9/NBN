import os
from typing import Dict, Any, List
import json
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.tools import tool
from sqlalchemy.orm import Session
from database import get_db
from models.inventory import Item
from .state import AgentState

llm = ChatGoogleGenerativeAI(
    model="gemini-2.5-flash-lite",
    google_api_key=os.getenv("GEMINI_API_KEY")
)

@tool
def update_inventory_database(name: str, quantity: int, price: float, description: str = "") -> str:
    """Updates the inventory database with new item details."""
    try:
        # Since this is a simple tool, we can grab a session directly
        db: Session = next(get_db())
        existing_item = db.query(Item).filter(Item.name == name).first()
        if existing_item:
            existing_item.quantity += quantity
            db.commit()
            return f"Updated existing item '{name}'. New quantity: {existing_item.quantity}."
        else:
            new_item = Item(
                name=name,
                quantity=quantity,
                price=price,
                description=description,
                show_on_website=True,
                available_on_whatsapp=True
            )
            db.add(new_item)
            db.commit()
            return f"Added new item '{name}' to the inventory with quantity {quantity}."
    except Exception as e:
        return f"Error updating database: {str(e)}"

# Bind tools to the LLM
ops_llm_with_tools = llm.bind_tools([update_inventory_database])

def ops_node(state: AgentState) -> Dict[str, Any]:
    system_prompt = """You are the Ops & ERP Specialist Agent.
    Your main job is to manage inventory, parse images of physical stock, and keep the database up to date.
    When users send you images or text about inventory, use your tools to update the database accordingly.
    Once done, reply with a summary of what you did.
    """
    
    messages = [SystemMessage(content=system_prompt)] + list(state["messages"])
    
    # Run the LLM
    response = ops_llm_with_tools.invoke(messages)
    
    actions = state.get("actions", [])
    
    # Handle tool calls if any
    if hasattr(response, "tool_calls") and response.tool_calls:
        for tool_call in response.tool_calls:
            if tool_call["name"] == "update_inventory_database":
                # Execute the tool
                tool_msg = update_inventory_database.invoke(tool_call)
                # Let the LLM observe the result to form a final answer
                messages.append(response)
                messages.append(tool_msg)
                final_response = llm.invoke(messages)
                response = final_response
                
                # Append a structured action for the Android UI
                actions.append({
                    "type": "inventory_alert",
                    "message": "Inventory updated successfully.",
                    "details": tool_call["args"]
                })
    
    return {"messages": [response], "actions": actions}

def process_vision_request(image_url: str) -> Dict[str, Any]:
    """Processes an image directly via Gemini Multimodal and extracts inventory data."""
    system_prompt = "You are an expert inventory scanner. Analyze the image and extract the item name, estimated price, and quantity. Return ONLY a valid JSON object with keys: name, price, quantity. If you cannot determine the quantity, default to 1. Do not use markdown blocks."
    
    message = HumanMessage(
        content=[
            {"type": "text", "text": system_prompt},
            {"type": "image_url", "image_url": image_url}
        ]
    )
    
    response = llm.invoke([message])
    
    try:
        data = json.loads(response.content.strip())
        return {
            "detected_item": data.get("name", "Unknown Item"),
            "estimated_price": float(data.get("price", 0.0)),
            "quantity": int(data.get("quantity", 1)),
            "status": "success"
        }
    except Exception as e:
        return {
            "detected_item": "Unknown",
            "estimated_price": 0,
            "quantity": 0,
            "status": f"error: {str(e)}",
            "raw_response": response.content
        }
