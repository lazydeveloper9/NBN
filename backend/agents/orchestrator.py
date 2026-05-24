import os
from typing import Dict, Any, Literal
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from langgraph.graph import StateGraph, END
from .state import AgentState
from .ops import ops_node
from .comms import comms_node
from .devops import devops_node

# Define the LLM
llm = ChatGoogleGenerativeAI(
    model="gemini-2.5-flash-lite",
    google_api_key=os.getenv("GEMINI_API_KEY")
)

def supervisor_node(state: AgentState) -> Dict[str, Any]:
    system_prompt = """You are the NBN CTO Agent Supervisor. 
    Your job is to route the user's request to the correct specialist.
    Available specialists:
    - DevOps: For creating web pages, deploying apps, cloud infrastructure.
    - Ops: For inventory management, physical goods, vision analysis, orders.
    - Comms: For WhatsApp messages, marketing, CRM.
    - FINISH: If the request is a general question you can answer directly.

    Respond with ONLY the name of the specialist or FINISH.
    """
    
    messages = [SystemMessage(content=system_prompt)] + list(state["messages"])
    response = llm.invoke(messages)
    
    # Simple routing logic
    decision = response.content.strip().upper()
    next_node = "FINISH"
    if "DEVOPS" in decision:
        next_node = "devops"
    elif "OPS" in decision:
        next_node = "ops"
    elif "COMMS" in decision:
        next_node = "comms"
        
    return {"next_node": next_node}

# Build the LangGraph
workflow = StateGraph(AgentState)

workflow.add_node("supervisor", supervisor_node)
workflow.add_node("devops", devops_node)
workflow.add_node("ops", ops_node)
workflow.add_node("comms", comms_node)

workflow.set_entry_point("supervisor")

# Routing edges
def route_next(state: AgentState) -> str:
    return state["next_node"] if state["next_node"] != "FINISH" else END

workflow.add_conditional_edges("supervisor", route_next)
workflow.add_edge("devops", END)
workflow.add_edge("ops", END)
workflow.add_edge("comms", END)

orchestrator_app = workflow.compile()
