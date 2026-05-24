import os
import sys
import traceback

# Mock GEMINI_API_KEY if not present so imports don't crash
if "GEMINI_API_KEY" not in os.environ:
    os.environ["GEMINI_API_KEY"] = "mock_key_for_testing"

# Try to import our app and build the graph
try:
    import main
    from agents.orchestrator import orchestrator_app
    from langchain_core.messages import HumanMessage
    
    print("✅ LangGraph Orchestrator compiled successfully.")
    
    # We won't actually invoke it if we are using a mock key because the API call will fail, 
    # but we can verify the structure
    nodes = orchestrator_app.nodes
    print("✅ Registered Nodes:", list(nodes.keys()))
    
    assert "supervisor" in nodes
    assert "ops" in nodes
    assert "devops" in nodes
    assert "comms" in nodes
    
except Exception as e:
    print(f"❌ Error during agent verification: {e}")
    traceback.print_exc()
    sys.exit(1)

# Verify FastAPI app loads
try:
    from main import app
    print("✅ FastAPI application loaded successfully.")
    print("✅ Registered Routes:", [route.path for route in app.routes])
except Exception as e:
    print(f"❌ Error during FastAPI verification: {e}")
    sys.exit(1)

print("\n🎉 All backend agent modules are structurally verified!")
