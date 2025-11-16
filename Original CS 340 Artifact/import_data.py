"""
Script to import CSV data into local MongoDB database
"""
import pandas as pd
from pymongo import MongoClient
import sys
import numpy as np

# Get CSV file path from command line argument or use default
csv_file = sys.argv[1] if len(sys.argv) > 1 else 'aac_shelter_outcomes.csv'

# Read the CSV file
print("Reading CSV file...")
try:
    df = pd.read_csv(csv_file)
    # Replace NaN values with empty strings to avoid 'nan' strings in database
    df = df.replace({np.nan: ''})
    print(f"Successfully loaded {len(df)} records from CSV")
except Exception as e:
    print(f"Error reading CSV: {e}")
    sys.exit(1)

# Connect to local MongoDB (no authentication needed for local)
print("\nConnecting to MongoDB...")
try:
    client = MongoClient('mongodb://localhost:27017/')
    db = client['AAC']
    collection = db['animals']
    print("Connected successfully!")
except Exception as e:
    print(f"Error connecting to MongoDB: {e}")
    sys.exit(1)

# Clear existing data (optional - comment out if you want to keep existing data)
print("\nClearing existing data...")
collection.delete_many({})

# Convert DataFrame to dictionary and insert
print("Inserting data into MongoDB...")
try:
    records = df.to_dict('records')
    result = collection.insert_many(records)
    print(f"Successfully inserted {len(result.inserted_ids)} documents!")
except Exception as e:
    print(f"Error inserting data: {e}")
    sys.exit(1)

# Verify the data
count = collection.count_documents({})
print(f"\nTotal documents in collection: {count}")

# Show sample document
print("\nSample document:")
sample = collection.find_one()
if sample:
    for key, value in sample.items():
        print(f"  {key}: {value}")

print("\nData import complete!")
print("\nNext steps:")
print("1. Update animal_shelter.py to use localhost instead of Apporto")
print("2. Run your dashboard notebook")
