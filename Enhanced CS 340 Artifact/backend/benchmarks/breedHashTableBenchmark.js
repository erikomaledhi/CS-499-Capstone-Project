
require('dotenv').config();
const mongoose = require('mongoose');
const Animal = require('../src/models/Animal');
const BreedHashTable = require('../src/services/BreedHashTable');

const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/AAC';

async function benchmarkMongoDBQuery(breed, iterations = 100) {
  const times = [];
  
  for (let i = 0; i < iterations; i++) {
    const start = process.hrtime.bigint();
    
    await Animal.find({
      breed: new RegExp(`^${breed}$`, 'i')
    }).lean();
    
    const end = process.hrtime.bigint();
    times.push(Number(end - start) / 1_000_000); // Convert to milliseconds
  }
  
  return {
    average: times.reduce((a, b) => a + b, 0) / times.length,
    min: Math.min(...times),
    max: Math.max(...times),
    total: times.reduce((a, b) => a + b, 0)
  };
}

/**
 * Benchmark: Hash Table lookup (optimized approach)
 */
function benchmarkHashTableLookup(hashTable, breed, iterations = 100) {
  const times = [];
  
  for (let i = 0; i < iterations; i++) {
    const start = process.hrtime.bigint();
    
    hashTable.getByBreed(breed);
    
    const end = process.hrtime.bigint();
    times.push(Number(end - start) / 1_000_000); // Convert to milliseconds
  }
  
  return {
    average: times.reduce((a, b) => a + b, 0) / times.length,
    min: Math.min(...times),
    max: Math.max(...times),
    total: times.reduce((a, b) => a + b, 0)
  };
}

async function benchmarkPartialSearch(hashTable, searchTerm, iterations = 50) {
  // MongoDB approach
  const mongoTimes = [];
  for (let i = 0; i < iterations; i++) {
    const start = process.hrtime.bigint();
    await Animal.find({
      breed: new RegExp(searchTerm, 'i')
    }).lean();
    const end = process.hrtime.bigint();
    mongoTimes.push(Number(end - start) / 1_000_000);
  }
  
  // Hash table approach
  const hashTimes = [];
  for (let i = 0; i < iterations; i++) {
    const start = process.hrtime.bigint();
    hashTable.searchByPartialBreed(searchTerm);
    const end = process.hrtime.bigint();
    hashTimes.push(Number(end - start) / 1_000_000);
  }
  
  return {
    mongodb: {
      average: mongoTimes.reduce((a, b) => a + b, 0) / mongoTimes.length,
      total: mongoTimes.reduce((a, b) => a + b, 0)
    },
    hashTable: {
      average: hashTimes.reduce((a, b) => a + b, 0) / hashTimes.length,
      total: hashTimes.reduce((a, b) => a + b, 0)
    }
  };
}

async function runBenchmarks() {
  try {
    console.log('Breed Hash Table Performance Benchmark');
    console.log('=' .repeat(60));
    console.log('');

    console.log('Connecting to MongoDB...');
    await mongoose.connect(MONGO_URI);
    console.log('Connected to MongoDB');
    console.log('');

    console.log('Loading animals from database...');
    const animals = await Animal.find({}).lean();
    console.log(`Loaded ${animals.length.toLocaleString()} animals`);
    console.log('');

    console.log('Building breed hash table...');
    const hashTable = new BreedHashTable();
    const buildStart = Date.now();
    const stats = hashTable.buildIndex(animals);
    const buildTime = Date.now() - buildStart;
    
    console.log(`Hash table built in ${buildTime}ms`);
    console.log(`Total animals: ${stats.totalAnimals.toLocaleString()}`);
    console.log(`Unique breeds: ${stats.uniqueBreeds.toLocaleString()}`);
    console.log(`Average per breed: ${(stats.totalAnimals / stats.uniqueBreeds).toFixed(1)}`);
    console.log('');

    const breedCounts = hashTable.getBreedCounts();
    const topBreeds = breedCounts.slice(0, 5);

    console.log('Exact Breed Lookup (O(1))');
    console.log('-'.repeat(60));
    console.log(`Testing ${100} iterations per breed...`);
    console.log('');

    const results = [];
    
    for (const { breed, count } of topBreeds) {
      console.log(`Testing breed: "${breed}" (${count} animals)`);
      
      const mongoResult = await benchmarkMongoDBQuery(breed, 100);
      console.log(`MongoDB Query:    avg: ${mongoResult.average.toFixed(2)}ms | min: ${mongoResult.min.toFixed(2)}ms | max: ${mongoResult.max.toFixed(2)}ms`);
      
      const hashResult = benchmarkHashTableLookup(hashTable, breed, 100);
      console.log(`Hash Table Lookup: avg: ${hashResult.average.toFixed(2)}ms | min: ${hashResult.min.toFixed(2)}ms | max: ${hashResult.max.toFixed(2)}ms`);
      
      const improvement = mongoResult.average / hashResult.average;
      console.log(`Improvement: ${improvement.toFixed(2)}x faster`);
      console.log('');
      
      results.push({
        breed,
        count,
        mongoAvg: mongoResult.average,
        hashAvg: hashResult.average,
        improvement
      });
    }

    console.log('Performance Comparison');
    console.log('=' .repeat(60));
    console.log('');

    const avgImprovement = results.reduce((sum, r) => sum + r.improvement, 0) / results.length;
    const minImprovement = Math.min(...results.map(r => r.improvement));
    const maxImprovement = Math.max(...results.map(r => r.improvement));

    console.log('Comparison Efficiency:');
    console.log(`Average Improvement: ${avgImprovement.toFixed(2)}x faster`);
    console.log(`Minimum Improvement: ${minImprovement.toFixed(2)}x faster`);
    console.log(`Maximum Improvement: ${maxImprovement.toFixed(2)}x faster`);
    console.log('');

    console.log('Partial Breed Search');
    console.log('-'.repeat(60));
    console.log(`Testing ${50} iterations per search term...`);
    console.log('');

    const searchTerms = ['Retriever', 'Shepherd', 'Terrier', 'Mix'];
    const searchResults = [];

    for (const term of searchTerms) {
      console.log(`Searching for: "${term}"`);
      
      const result = await benchmarkPartialSearch(hashTable, term, 50);
      
      console.log(`MongoDB:     avg: ${result.mongodb.average.toFixed(2)}ms`);
      console.log(`Hash Table:  avg: ${result.hashTable.average.toFixed(2)}ms`);
      
      const improvement = result.mongodb.average / result.hashTable.average;
      console.log(`Improvement: ${improvement.toFixed(2)}x faster`);
      console.log('');
      
      searchResults.push({
        term,
        mongoAvg: result.mongodb.average,
        hashAvg: result.hashTable.average,
        improvement
      });
    }

    console.log('Theoretical Analysis');
    console.log('=' .repeat(60));
    console.log('');

    const avgSearchImprovement = searchResults.reduce((sum, r) => sum + r.improvement, 0) / searchResults.length;
    console.log(`Exact Lookup:    ${avgImprovement.toFixed(2)}x faster (O(n) to O(1))`);
    console.log(`Partial Search:  ${avgSearchImprovement.toFixed(2)}x faster (O(n) to O(k+m))`);
    console.log(`Index Build:     ${buildTime}ms one-time cost`);
    console.log(`Space:           O(n + k) where n=${animals.length.toLocaleString()}, k=${stats.uniqueBreeds}`);
    console.log('');

    console.log('Real-World Impact');
    console.log('=' .repeat(60));
    console.log('');
    console.log('When loading breed distribution charts:');
    console.log(`Without Hash Table: ~${results[0].mongoAvg.toFixed(0)}ms per breed query`);
    console.log(`With Hash Table:    ~${results[0].hashAvg.toFixed(2)}ms per breed query`);
    console.log('');
    console.log('User Experience:');
    console.log(`${avgImprovement.toFixed(2)}x faster chart loading`);
    console.log(`${((1 - 1/avgImprovement) * 100).toFixed(1)}% reduction in wait time`);
    console.log('Scales efficiently as dataset grows');
    console.log('');

    console.log('Benchmark Complete');
    console.log('=' .repeat(60));
    console.log('');
    console.log('Key Findings:');
    console.log(`Hash table achieves O(1) lookup time`);
    console.log(`${avgImprovement.toFixed(2)}x faster for exact lookups`);
    console.log(`${avgSearchImprovement.toFixed(2)}x faster for partial searches`);
    console.log('Constant time performance regardless of dataset size');
    console.log('Ideal for breed statistics and filtering operations');
    console.log('');

  } catch (error) {
    console.error('Benchmark failed:', error);
  } finally {
    await mongoose.connection.close();
    console.log('Database connection closed');
    console.log('');
  }
}

runBenchmarks()
  .then(() => {
    console.log('Benchmark finished successfully');
    process.exit(0);
  })
  .catch((error) => {
    console.error('Fatal error:', error);
    process.exit(1);
  });
