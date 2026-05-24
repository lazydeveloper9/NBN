import os
import weaviate
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base

# PostgreSQL Setup
SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./nbn_local.db") # Fallback for local testing

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, 
    connect_args={"check_same_thread": False} if "sqlite" in SQLALCHEMY_DATABASE_URL else {}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Vector DB (Weaviate) Setup
def get_weaviate_client():
    weaviate_url = os.getenv("WEAVIATE_URL", "http://localhost:8080")
    # For local development we skip API keys, but in production we'd use WCS (Weaviate Cloud)
    client = weaviate.Client(url=weaviate_url)
    return client

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
