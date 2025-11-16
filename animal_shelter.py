from pymongo import MongoClient
from bson.objectid import ObjectId

class AnimalShelter:
    """ CRUD operations for Animal collection in MongoDB """

    def __init__(self, user=None, password=None):
        # Connection Variables for LOCAL MongoDB
        HOST = 'localhost'
        PORT = 27017
        DB = 'AAC'
        COL = 'animals'
        # Initialize Connection (no authentication needed for local MongoDB by default)
        self.client = MongoClient('mongodb://%s:%d' % (HOST, PORT))
        self.database = self.client[DB]
        self.collection = self.database[COL]

    def create(self, data):
        """Insert a document into the collection"""
        try:
            if data is not None and isinstance(data, dict):
                result = self.collection.insert_one(data)
                return True if result.inserted_id else False
            else:
                raise Exception("Data parameter must be a non-empty dictionary")
        except Exception as e:
            print(f"Error in create: {e}")
            return False

    def read(self, query=None, projection=None):
        """Query documents from the collection"""
        try:
            if query is None:
                query = {}
            
            if not isinstance(query, dict):
                raise Exception("Query parameter must be a dictionary")
            
            if projection is not None and not isinstance(projection, dict):
                raise Exception("Projection parameter must be a dictionary")
            
            cursor = self.collection.find(query, projection)
            return [doc for doc in cursor]
        except Exception as e:
            print(f"Error in read: {e}")
            return []
     
    def update(self, query, update_data):
        """Update documents in the collection"""
        try:
            if query is not None and isinstance(query, dict) and update_data is not None and isinstance(update_data, dict):
                result = self.collection.update_many(query, {"$set": update_data})
                return result.modified_count
            else:
                raise Exception("Query and update_data parameters must be non-empty dictionaries")
        except Exception as e:
            print(f"Error in update: {e}")
            return 0

    def delete(self, query):
        """Delete documents from the collection that match the query"""
        try:
            if query is not None and isinstance(query, dict):
                result = self.collection.delete_many(query)
                return result.deleted_count
            else:
                raise Exception("Query parameter must be a non-empty dictionary")
        except Exception as e:
            print(f"Error in delete: {e}")
            return 0