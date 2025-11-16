
### Prerequisites
- **Node.js** (v18.0.0 or higher)
- **MongoDB** (v5.0 or higher)
- **npm** (v9.0.0 or higher)

### Installation


#### 1. Backend Setup
```powershell
cd backend

# Install dependencies
npm install

# Create .env file from example
Copy-Item .env.example .env


# Start the server
npm run dev
```

The backend will run on `http://localhost:5000`

#### 2. Frontend Setup
```powershell
cd frontend

# Install dependencies
npm install

# Create .env file
Copy-Item .env.example .env

# Start development server
npm run dev
```

The frontend will run on `http://localhost:3000`

#### 3. Database Setup

**Option 1: Using the Import Script**

Run the Node.js import script:
```powershell
# Import CSV to MongoDB (from project root)
node import_data.js aac_shelter_outcomes.csv
```

**Option 2: Using mongoimport**

```powershelll
mongoimport --db AAC --collection animals --type csv --headerline --file aac_shelter_outcomes.csv
```



