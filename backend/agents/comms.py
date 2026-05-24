import os
import json
import logging
from typing import Dict, Any
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.tools import tool
from .state import AgentState

logger = logging.getLogger(__name__)

llm = ChatGoogleGenerativeAI(
    model="gemini-2.5-flash-lite",
    google_api_key=os.getenv("GEMINI_API_KEY")
)

@tool
def send_whatsapp_message(phone_number: str, message: str) -> str:
    """Sends a WhatsApp message to a customer via the WhatsApp Business API."""
    # In a real environment, this would call the WhatsApp Graph API:
    # url = f"https://graph.facebook.com/v17.0/{PHONE_NUMBER_ID}/messages"
    # headers = {"Authorization": f"Bearer {WHATSAPP_TOKEN}", "Content-Type": "application/json"}
    # payload = { "messaging_product": "whatsapp", "to": phone_number, "type": "text", "text": { "body": message } }
    # response = requests.post(url, headers=headers, json=payload)
    
    logger.info(f"[WhatsApp Simulator] Sent to {phone_number}: {message}")
    return f"Message successfully delivered to {phone_number}."

@tool
def bulk_message_customers(customer_segment: str, message_template: str) -> str:
    """Sends a bulk marketing message to a specific segment of customers (e.g. 'recent_buyers', 'inactive')."""
    # Simulate DB lookup for segment
    simulated_count = 15 if customer_segment == "recent_buyers" else 42
    logger.info(f"[WhatsApp Simulator] Sent bulk message to {simulated_count} customers in segment '{customer_segment}'. Template: {message_template}")
    return f"Bulk message successfully dispatched to {simulated_count} customers."

# Bind tools to the LLM
comms_llm_with_tools = llm.bind_tools([send_whatsapp_message, bulk_message_customers])

def comms_node(state: AgentState) -> Dict[str, Any]:
    system_prompt = """You are the Comms & Marketing Specialist Agent.
    Your job is to manage customer relationships, handle incoming WhatsApp inquiries, and run outbound marketing campaigns.
    When a user asks you to message someone or run a campaign, use your tools to execute the action via the WhatsApp Business API.
    Always summarize your actions clearly.
    """
    
    messages = [SystemMessage(content=system_prompt)] + list(state["messages"])
    
    # Run the LLM
    response = comms_llm_with_tools.invoke(messages)
    
    actions = state.get("actions", [])
    
    # Handle tool calls if any
    if hasattr(response, "tool_calls") and response.tool_calls:
        for tool_call in response.tool_calls:
            # Execute the correct tool dynamically
            if tool_call["name"] == "send_whatsapp_message":
                tool_msg = send_whatsapp_message.invoke(tool_call)
            elif tool_call["name"] == "bulk_message_customers":
                tool_msg = bulk_message_customers.invoke(tool_call)
            else:
                continue
                
            # Let the LLM observe the result to form a final answer
            messages.append(response)
            messages.append(tool_msg)
            final_response = llm.invoke(messages)
            response = final_response
            
            # Append a structured action for the Android UI
            actions.append({
                "type": "comms_update",
                "message": "WhatsApp action executed.",
                "details": tool_call["args"]
            })
    
    return {"messages": [response], "actions": actions}
