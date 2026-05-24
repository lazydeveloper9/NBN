from typing import TypedDict, Annotated, Sequence, List, Dict, Any
from langchain_core.messages import BaseMessage
import operator

class AgentState(TypedDict):
    messages: Annotated[Sequence[BaseMessage], operator.add]
    user_id: str
    next_node: str # The agent to route to
    actions: List[Dict[str, Any]] # Structured UI actions to return to Android
