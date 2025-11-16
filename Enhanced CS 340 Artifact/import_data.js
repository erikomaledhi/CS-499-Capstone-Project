/**
 * Import Animal Shelter Data to MongoDB
 * ======================================
 * This script imports the animal shelter CSV data into your local MongoDB database.
 * 
 * Usage:
 *     node import_data.js aac_shelter_outcomes.csv
 */

const fs = require('fs');
const path = require('path');
const readline = require('readline');
const { MongoClient } = require('mongodb');

// CSV parsing function
function parseCSV(csvText) {
    const lines = csvText.split('\n');
    const headers = lines[0].split(',').map(h => h.trim().replace(/^"|"$/g, ''));
    const records = [];

    for (let i = 1; i < lines.length; i++) {
        const line = lines[i].trim();
        if (!line) continue;

        const values = line.split(',').map(v => v.trim().replace(/^"|"$/g, ''));
        const record = {};

        headers.forEach((header, index) => {
            const value = values[index] || '';
            record[header] = value === 'NaN' || value === '' ? '' : value;
        });

        records.push(record);
    }

    return records;
}

// Prompt user for yes/no
function prompt(question) {
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });

    return new Promise((resolve) => {
        rl.question(question, (answer) => {
            rl.close();
            resolve(answer.trim().toLowerCase());
        });
    });
}

async function main() {
    // Get CSV file path from command line argument or use default
    const csvFile = process.argv[2] || 'aac_shelter_outcomes_fixed.csv';
    
    console.log('='.repeat(60));
    console.log('Animal Shelter Data Import Tool');
    console.log('='.repeat(60));

    // Check if file exists
    if (!fs.existsSync(csvFile)) {
        console.log(`\n✗ Error: CSV file '${csvFile}' not found!`);
        console.log('\nUsage: node import_data.js [csv_file_path]');
        console.log('Example: node import_data.js aac_shelter_outcomes_fixed.csv');
        process.exit(1);
    }

    // Read the CSV file
    console.log(`\n[1/4] Reading CSV file: ${csvFile}`);
    let records;
    try {
        const csvText = fs.readFileSync(csvFile, 'utf-8');
        records = parseCSV(csvText);
        console.log(`✓ Successfully loaded ${records.length} records from CSV`);
    } catch (error) {
        console.log(`✗ Error reading CSV: ${error.message}`);
        process.exit(1);
    }

    // Connect to MongoDB
    console.log('\n[2/4] Connecting to MongoDB...');
    const mongodbUri = 'mongodb://localhost:27017/';
    const databaseName = 'AAC';
    const collectionName = 'animals';

    let client;
    try {
        client = new MongoClient(mongodbUri);
        await client.connect();
        await client.db().admin().ping();
        
        const db = client.db(databaseName);
        const collection = db.collection(collectionName);
        
        console.log(`✓ Connected to MongoDB at ${mongodbUri}`);
        console.log(`  Database: ${databaseName}`);
        console.log(`  Collection: ${collectionName}`);

        // Check for existing data
        const existingCount = await collection.countDocuments({});
        if (existingCount > 0) {
            console.log(`\n⚠ Warning: Collection already contains ${existingCount} documents`);
            const response = await prompt('Do you want to clear existing data before importing? (yes/no): ');
            
            if (response === 'yes' || response === 'y') {
                console.log('\n[3/4] Clearing existing data...');
                await collection.deleteMany({});
                console.log(`✓ Removed ${existingCount} existing documents`);
            } else {
                console.log('\n[3/4] Keeping existing data (new data will be added)');
            }
        } else {
            console.log('\n[3/4] Collection is empty, ready for import');
        }

        // Insert data
        console.log('\n[4/4] Inserting data into MongoDB...');
        const result = await collection.insertMany(records);
        console.log(`✓ Successfully inserted ${result.insertedCount} documents!`);

        // Verify the data
        const finalCount = await collection.countDocuments({});
        console.log('\n' + '='.repeat(60));
        console.log('Import Complete!');
        console.log('='.repeat(60));
        console.log(`Total documents in collection: ${finalCount}`);

        // Show sample document
        console.log('\nSample document from database:');
        console.log('-'.repeat(60));
        const sample = await collection.findOne({});
        if (sample) {
            Object.entries(sample).forEach(([key, value]) => {
                if (key !== '_id') {  // Skip MongoDB's internal ID
                    console.log(`  ${key}: ${value}`);
                }
            });
        }

        console.log('\n' + '='.repeat(60));
        console.log('Next Steps:');
        console.log('='.repeat(60));
        console.log('1. Start the backend server:');
        console.log('   cd backend');
        console.log('   npm run dev');
        console.log('\n2. Start the frontend server:');
        console.log('   cd frontend');
        console.log('   npm run dev');
        console.log('\n3. Open your browser to http://localhost:3000');
        console.log('='.repeat(60));

        await client.close();

    } catch (error) {
        console.log(`✗ Error: ${error.message}`);
        if (error.message.includes('connect')) {
            console.log('\nMake sure MongoDB is running on your system:');
            console.log('  Windows: Check if MongoDB service is running');
            console.log('  Mac/Linux: Run "mongod" in a terminal');
        }
        if (client) await client.close();
        process.exit(1);
    }
}

main();
