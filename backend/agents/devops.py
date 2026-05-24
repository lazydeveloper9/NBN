import os
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
def generate_and_deploy_pwa(campaign_name: str, headline: str, product_ids: str) -> str:
    """Generates a static PWA landing page for a campaign and deploys it to a static S3 bucket."""
    # Simulate generating the HTML content
    html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{campaign_name}</title>
    <style>
        body {{ font-family: sans-serif; text-align: center; padding: 50px; }}
        h1 {{ color: #2A5D34; }}
    </style>
</head>
<body>
    <h1>{headline}</h1>
    <p>Check out our featured products: {product_ids}</p>
</body>
</html>"""

    # In a real environment, we would use boto3 to upload to S3:
    # s3_client = boto3.client('s3')
    # s3_client.put_object(Body=html_content, Bucket='nbn-pwa-deployments', Key=f'{campaign_name}/index.html', ContentType='text/html')
    
    deployment_url = f"https://nbn-deploy.msme.local/{campaign_name.lower().replace(' ', '-')}"
    logger.info(f"[DevOps Simulator] Deployed PWA to {deployment_url}")
    
    return f"Successfully generated and deployed PWA. Available at {deployment_url}"

# Bind tools to the LLM
devops_llm_with_tools = llm.bind_tools([generate_and_deploy_pwa])

def devops_node(state: AgentState) -> Dict[str, Any]:
    system_prompt = """You are the DevOps Specialist Agent.
    Your job is to manage technical infrastructure, generate code, and deploy web applications on demand.
    When a user asks to spin up a new page, website, or campaign portal, use your tools to generate the HTML/JS and deploy it.
    Always summarize your actions clearly.
    """
    
    messages = [SystemMessage(content=system_prompt)] + list(state["messages"])
    
    # Run the LLM
    response = devops_llm_with_tools.invoke(messages)
    
    actions = state.get("actions", [])
    
    # Handle tool calls if any
    if hasattr(response, "tool_calls") and response.tool_calls:
        for tool_call in response.tool_calls:
            if tool_call["name"] == "generate_and_deploy_pwa":
                tool_msg = generate_and_deploy_pwa.invoke(tool_call)
                
                # Let the LLM observe the result to form a final answer
                messages.append(response)
                messages.append(tool_msg)
                final_response = llm.invoke(messages)
                response = final_response
                
                # Append a structured action for the Android UI
                url_slug = tool_call["args"].get("campaign_name", "campaign").lower().replace(' ', '-')
                actions.append({
                    "type": "website_preview",
                    "url": f"https://nbn-deploy.msme.local/{url_slug}",
                    "message": "DevOps successfully deployed the PWA.",
                    "details": tool_call["args"]
                })
    
    return {"messages": [response], "actions": actions}
