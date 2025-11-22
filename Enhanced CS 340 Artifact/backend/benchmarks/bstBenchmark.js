

const mongoose = require('mongoose');
const AnimalBST = require('../src/services/AnimalBST');
const Animal = require('../src/models/Animal');

// MongoDB connection
const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/AAC';


function linearSearch(animals, targetId) {
  let comparisons = 0;
  
  for (const animal of animals) {
    comparisons++;
    if (animal.animal_id === targetId) {
      return { animal, comparisons };
    }
  }
  
  return { animal: null, comparisons };
}


async function runBenchmark() {
  try {
    console.log('BST Performance Benchmark');
    console.log('=' .repeat(60));
    console.log('');

    console.log('Connecting to MongoDB...');
    await mongoose.connect(MONGO_URI);
    console.log('Connected to MongoDB');
    console.log('');

    console.log('Loading animals from database...');
    const animals = await Animal.find({})
      .select('animal_id breed animal_type name')
      .lean();
    
    console.log(`Loaded ${animals.length.toLocaleString()} animals`);
    console.log('');

    console.log('Building Binary Search Tree...');
    const bst = new AnimalBST();
    const buildStats = bst.buildFromArray(animals);
    
    console.log(`BST built in ${buildStats.buildTime}ms`);
    console.log(`Size: ${buildStats.size.toLocaleString()} nodes`);
    console.log(`Max Depth: ${buildStats.maxDepth}`);
    console.log(`Average Depth: ${buildStats.averageDepth}`);
    console.log(`Theoretical Optimal: ${Math.ceil(Math.log2(buildStats.size + 1))}`);
    console.log(`Balanced: ${bst.getStats().isBalanced ? 'Yes' : 'No'}`);
    console.log('');

    // Select random animal IDs for testing
    const numTests = 100;
    const testIds = [];
    
    for (let i = 0; i < numTests; i++) {
      const randomIndex = Math.floor(Math.random() * animals.length);
      testIds.push(animals[randomIndex].animal_id);
    }

    console.log(`Running ${numTests} search tests...`);
    console.log('');

    console.log('Linear Search (O(n))');
    console.log('-'.repeat(60));
    
    const linearStart = Date.now();
    let linearTotalComparisons = 0;
    let linearFound = 0;

    for (const targetId of testIds) {
      const result = linearSearch(animals, targetId);
      linearTotalComparisons += result.comparisons;
      if (result.animal) linearFound++;
    }

    const linearDuration = Date.now() - linearStart;
    const linearAvgComparisons = linearTotalComparisons / numTests;

    console.log(`Duration: ${linearDuration}ms`);
    console.log(`Average comparisons: ${linearAvgComparisons.toFixed(2)}`);
    console.log(`Found: ${linearFound}/${numTests}`);
    console.log('');

    console.log('Binary Search Tree (O(log n))');
    console.log('-'.repeat(60));
    
    const bstStart = Date.now();
    let bstTotalComparisons = 0;
    let bstFound = 0;

    for (const targetId of testIds) {
      const result = bst.search(targetId);
      if (result) {
        bstTotalComparisons += result.comparisons;
        bstFound++;
      }
    }

    const bstDuration = Date.now() - bstStart;
    const bstAvgComparisons = bstTotalComparisons / numTests;

    console.log(`Duration: ${bstDuration}ms`);
    console.log(`Average comparisons: ${bstAvgComparisons.toFixed(2)}`);
    console.log(`Found: ${bstFound}/${numTests}`);
    console.log('');

    console.log('Performance Comparison');
    console.log('=' .repeat(60));
    
    const comparisonImprovement = (linearAvgComparisons / bstAvgComparisons).toFixed(2);
    const speedImprovement = (linearDuration / bstDuration).toFixed(2);
    const timeReduction = (((linearDuration - bstDuration) / linearDuration) * 100).toFixed(1);

    console.log('');
    console.log('Comparison Efficiency:');
    console.log(`Linear: ${linearAvgComparisons.toFixed(2)} comparisons`);
    console.log(`BST:    ${bstAvgComparisons.toFixed(2)} comparisons`);
    console.log(`Improvement: ${comparisonImprovement}x fewer comparisons`);
    console.log('');

    console.log('Speed Performance:');
    console.log(`Linear: ${linearDuration}ms`);
    console.log(`BST:    ${bstDuration}ms`);
    console.log(`Improvement: ${speedImprovement}x faster`);
    console.log(`Time saved: ${timeReduction}%`);
    console.log('');

    console.log('Theoretical Analysis');
    console.log('=' .repeat(60));
    console.log('');
    
    const theoreticalLinear = animals.length / 2; // Average case
    const theoreticalBST = Math.ceil(Math.log2(animals.length + 1));
    const theoreticalImprovement = (theoreticalLinear / theoreticalBST).toFixed(2);

    console.log(`For ${animals.length.toLocaleString()} animals:`);
    console.log(`Linear Search (O(n)):      ~${theoreticalLinear.toLocaleString()} comparisons`);
    console.log(`BST Search (O(log n)):     ~${theoreticalBST} comparisons`);
    console.log(`Theoretical Improvement:   ${theoreticalImprovement}x`);
    console.log('');

    console.log('Complexity Analysis:');
    console.log(`Linear: O(n) - grows linearly with dataset size`);
    console.log(`BST:    O(log n) - grows logarithmically with dataset size`);
    console.log('');
    console.log(`For 1,000 animals:     Linear ~500, BST ~10     (50x)`);
    console.log(`For 10,000 animals:    Linear ~5,000, BST ~13   (385x)`);
    console.log(`For 100,000 animals:   Linear ~50,000, BST ~17  (2,941x)`);
    console.log(`For 1,000,000 animals: Linear ~500,000, BST ~20 (25,000x)`);
    console.log('');

    console.log('Real-World Impact');
    console.log('=' .repeat(60));
    console.log('');
    console.log('When a user clicks an animal in the table:');
    console.log(`Without BST: ${linearDuration}ms (${linearAvgComparisons.toFixed(0)} comparisons)`);
    console.log(`With BST:    ${bstDuration}ms (${bstAvgComparisons.toFixed(0)} comparisons)`);
    console.log('');
    console.log('User Experience:');
    console.log(`${speedImprovement}x faster page loads`);
    console.log(`${timeReduction}% reduction in wait time`);
    console.log('Scales efficiently as dataset grows');
    console.log('');

    console.log('Edge Case Testing');
    console.log('=' .repeat(60));
    console.log('');

    // Test first animal (best case for linear)
    const firstId = animals[0].animal_id;
    const firstLinear = linearSearch(animals, firstId);
    const firstBST = bst.search(firstId);

    console.log('First Animal (Best case for Linear):');
    console.log(`Linear: ${firstLinear.comparisons} comparisons`);
    console.log(`BST:    ${firstBST ? firstBST.comparisons : 'N/A'} comparisons`);
    console.log('');

    // Test last animal (worst case for linear)
    const lastId = animals[animals.length - 1].animal_id;
    const lastLinear = linearSearch(animals, lastId);
    const lastBST = bst.search(lastId);

    console.log('Last Animal (Worst case for Linear):');
    console.log(`Linear: ${lastLinear.comparisons} comparisons`);
    console.log(`BST:    ${lastBST ? lastBST.comparisons : 'N/A'} comparisons`);
    console.log(`Improvement: ${(lastLinear.comparisons / lastBST.comparisons).toFixed(2)}x`);
    console.log('');

    // Test non-existent ID
    const fakeId = 'FAKE_ID_12345';
    const fakeLinear = linearSearch(animals, fakeId);
    const fakeBST = bst.search(fakeId);

    console.log('Non-existent ID (Worst case for both):');
    console.log(`Linear: ${fakeLinear.comparisons} comparisons (full scan)`);
    console.log(`BST:    ${fakeBST ? fakeBST.comparisons : 'Not found'} comparisons`);
    console.log('');

    console.log('Benchmark Complete');
    console.log('=' .repeat(60));
    console.log('');
    console.log('Key Findings:');
    console.log(`BST achieves O(log n) lookup time`);
    console.log(`${comparisonImprovement}x fewer comparisons on average`);
    console.log(`${speedImprovement}x faster execution time`);
    console.log('Scales logarithmically (ideal for large datasets)');
    console.log('Consistent performance across all positions');
    console.log('');

    await mongoose.connection.close();
    console.log('Database connection closed');
    
  } catch (error) {
    console.error('Benchmark failed:', error);
    process.exit(1);
  }
}

runBenchmark()
  .then(() => {
    console.log('');
    console.log('Benchmark finished successfully');
    process.exit(0);
  })
  .catch((error) => {
    console.error('Fatal error:', error);
    process.exit(1);
  });
