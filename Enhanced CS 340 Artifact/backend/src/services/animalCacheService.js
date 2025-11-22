/**
 * Animal Cache Service
 * 
 * Manages in-memory caching of animal data using optimized data structures
 * for high-performance queries.
 * 
 * Features:
 * - Breed Hash Table for O(1) breed lookups
 * - Automatic cache building on server startup
 * - Cache invalidation on data modifications
 * - Performance monitoring and statistics
 */

const BreedHashTable = require('./BreedHashTable');
const NameHashTable = require('./NameHashTable');
const AnimalBST = require('./AnimalBST');
const Animal = require('../models/Animal');

class AnimalCacheService {
  constructor() {
    this.breedIndex = new BreedHashTable();
    this.nameIndex = new NameHashTable();
    this.bstIndex = new AnimalBST(); // Binary Search Tree for O(log n) ID lookups
    this.isInitialized = false;
    this.initializationPromise = null;
  }

  /**
   * Initialize the cache by building indexes from database
   * @returns {Promise<Object>} Initialization statistics
   */
  async initialize() {
    // Prevent multiple simultaneous initializations
    if (this.initializationPromise) {
      return this.initializationPromise;
    }

    if (this.isInitialized) {
      return { message: 'Already initialized', stats: this.breedIndex.getStats() };
    }

    this.initializationPromise = this._buildCache();
    
    try {
      const result = await this.initializationPromise;
      this.isInitialized = true;
      return result;
    } finally {
      this.initializationPromise = null;
    }
  }

  /**
   * Build cache from database
   * @private
   */
  async _buildCache() {
    const startTime = Date.now();
    
    try {
      console.log('Building animal cache...');
      
      // Fetch all animals from database
      // Only fetch necessary fields to reduce memory usage
      const animals = await Animal.find({})
        .select('animal_id breed animal_type name sex_upon_outcome age_upon_outcome_in_weeks location_lat location_long datetime')
        .lean(); // Use lean() for better performance (returns plain JS objects)
      
      // Build breed index
      const breedStats = this.breedIndex.buildIndex(animals);
      
      // Build name index
      const nameStats = this.nameIndex.buildIndex(animals);
      
      // Build BST index for animal ID lookups
      const bstStats = this.bstIndex.buildFromArray(animals);
      
      const duration = Date.now() - startTime;
      
      console.log(`Cache built successfully in ${duration}ms`);
      console.log(`Total animals: ${breedStats.totalAnimals}`);
      console.log(`Unique breeds: ${breedStats.uniqueBreeds}`);
      console.log(`Unique names: ${nameStats.uniqueNames}`);
      console.log(`BST depth: ${bstStats.averageDepth} avg, ${bstStats.maxDepth} max`);
      
      return {
        success: true,
        duration,
        breedStats,
        nameStats,
        bstStats
      };
    } catch (error) {
      console.error('Cache build failed:', error);
      throw error;
    }
  }

  /**
   * Rebuild the entire cache
   * @returns {Promise<Object>} Rebuild statistics
   */
  async rebuild() {
    console.log('Rebuilding cache...');
    this.isInitialized = false;
    this.breedIndex.clear();
    this.nameIndex.clear();
    this.bstIndex.clear();
    return this.initialize();
  }

  /**
   * Get animals by breed using hash table (O(1) lookup)
   * @param {String} breed - Breed name
   * @returns {Array} Array of animal objects
   */
  getByBreed(breed) {
    if (!this.isInitialized) {
      throw new Error('Cache not initialized. Call initialize() first.');
    }
    
    return this.breedIndex.getByBreed(breed);
  }

  /**
   * Get animals by multiple breeds
   * @param {Array} breeds - Array of breed names
   * @returns {Array} Combined array of animal objects
   */
  getByBreeds(breeds) {
    if (!this.isInitialized) {
      throw new Error('Cache not initialized. Call initialize() first.');
    }
    
    return this.breedIndex.getByBreeds(breeds);
  }

  /**
   * Search animals by partial breed name
   * @param {String} partialBreed - Partial breed name
   * @returns {Array} Array of animal objects
   */
  searchByPartialBreed(partialBreed) {
    if (!this.isInitialized) {
      throw new Error('Cache not initialized. Call initialize() first.');
    }
    
    return this.breedIndex.searchByPartialBreed(partialBreed);
  }

  /**
   * Get all unique breeds
   * @returns {Array} Sorted array of breed names
   */
  getAllBreeds() {
    if (!this.isInitialized) {
      throw new Error('Cache not initialized. Call initialize() first.');
    }
    
    return this.breedIndex.getAllBreeds();
  }

  /**
   * Get breed statistics
   * @returns {Array} Array of {breed, count} objects
   */
  getBreedCounts() {
    if (!this.isInitialized) {
      throw new Error('Cache not initialized. Call initialize() first.');
    }
    
    return this.breedIndex.getBreedCounts();
  }

  /**
   * Get animals by exact name
   * @param {String} name - Animal name
   * @returns {Array} Array of animal objects
   */
  getByName(name) {
    if (!this.isInitialized) {
      throw new Error('Cache not initialized. Call initialize() first.');
    }
    
    return this.nameIndex.getByName(name);
  }

  /**
   * Search animals by partial name
   * @param {String} partialName - Partial name to search
   * @returns {Array} Array of animal objects
   */
  searchByPartialName(partialName) {
    if (!this.isInitialized) {
      throw new Error('Cache not initialized. Call initialize() first.');
    }
    
    return this.nameIndex.searchByPartialName(partialName);
  }

  /**
   * Search for animal by ID using BST (O(log n) lookup)
   * @param {String} animalId - Animal ID to search
   * @returns {Object|null} Animal object with comparison count, or null if not found
   */
  searchById(animalId) {
    if (!this.isInitialized) {
      throw new Error('Cache not initialized. Call initialize() first.');
    }
    
    return this.bstIndex.search(animalId);
  }

  /**
   * Handle animal creation - add to cache
   * @param {Object} animal - New animal object
   */
  onAnimalCreated(animal) {
    if (this.isInitialized) {
      this.breedIndex.addAnimal(animal);
      this.nameIndex.addAnimal(animal);
      this.bstIndex.insert(animal);
    }
  }

  /**
   * Handle animal update - update in cache
   * @param {String} animalId - Animal ID
   * @param {Object} updatedAnimal - Updated animal object
   */
  onAnimalUpdated(animalId, updatedAnimal) {
    if (this.isInitialized) {
      this.breedIndex.updateAnimal(animalId, updatedAnimal);
      this.nameIndex.updateAnimal(animalId, updatedAnimal);
      this.bstIndex.update(animalId, updatedAnimal);
    }
  }

  /**
   * Handle animal deletion - remove from cache
   * @param {String} animalId - Animal ID to remove
   */
  onAnimalDeleted(animalId) {
    if (this.isInitialized) {
      this.breedIndex.removeAnimal(animalId);
      this.nameIndex.removeAnimal(animalId);
      this.bstIndex.delete(animalId);
    }
  }

  /**
   * Get cache statistics
   * @returns {Object} Statistics object
   */
  getStats() {
    return {
      initialized: this.isInitialized,
      breedIndex: this.breedIndex.getStats(),
      nameIndex: this.nameIndex.getStats(),
      bstIndex: this.bstIndex.getStats()
    };
  }

  /**
   * Check if cache is initialized
   * @returns {Boolean}
   */
  isReady() {
    return this.isInitialized;
  }
}

// Create singleton instance
const cacheService = new AnimalCacheService();

module.exports = cacheService;
