import weaviate
from weaviate.classes.config import Property, DataType

def initialize_weaviate_schema(client: weaviate.Client):
    # Memory for the Orchestrator agent to remember past decisions and business context
    if not client.collections.exists("AgentMemory"):
        client.collections.create(
            name="AgentMemory",
            properties=[
                Property(name="content", data_type=DataType.TEXT),
                Property(name="agent_type", data_type=DataType.TEXT),
                Property(name="timestamp", data_type=DataType.DATE),
            ]
        )
    
    # Store unstructured inventory details or SOPs
    if not client.collections.exists("BusinessKnowledge"):
        client.collections.create(
            name="BusinessKnowledge",
            properties=[
                Property(name="topic", data_type=DataType.TEXT),
                Property(name="content", data_type=DataType.TEXT),
            ]
        )
