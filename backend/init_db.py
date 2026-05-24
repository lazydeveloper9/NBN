from database import engine, Base, get_weaviate_client
from models.inventory import Item
from weaviate_schema import initialize_weaviate_schema

def init_postgres():
    print("Creating PostgreSQL tables...")
    Base.metadata.create_all(bind=engine)
    print("PostgreSQL tables created.")

def init_weaviate():
    print("Initializing Weaviate collections...")
    client = get_weaviate_client()
    try:
        # Check if connected
        client.connect()
        initialize_weaviate_schema(client)
        print("Weaviate collections initialized.")
    except Exception as e:
        print(f"Failed to connect to Weaviate: {e}")
    finally:
        client.close()

if __name__ == "__main__":
    init_postgres()
    init_weaviate()
