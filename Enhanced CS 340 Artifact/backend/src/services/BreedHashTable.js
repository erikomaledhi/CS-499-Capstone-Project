/**
 * BreedHashTable - High-performance breed lookup using hash table
 */

class BreedHashTable {
  constructor() {
    // Hash table: key = breed name (lowercase), value = array of animal objects
    this.index = {};
    
    // Track statistics for performance monitoring
    this.stats = {
      totalAnimals: 0,
      uniqueBreeds: 0,
      lastBuildTime: null,
      buildDuration: 0
    };
  }

  /**
   * Build the breed index from an array of animals
   * @param {Array} animals - Array of animal objects from MongoDB
   * @returns {Object} Index statistics
   * 
   */
  buildIndex(animals) {
    const startTime = Date.now();
    
    // Clear existing index
    this.index = {};
    
    // Build new index
    animals.forEach(animal => {
      const breed = animal.breed ? animal.breed.toLowerCase().trim() : 'unknown';
      
      // If breed doesn't exist in index, create empty array
      if (!this.index[breed]) {
        this.index[breed] = [];
      }
      
      // Add animal to breed's array
      this.index[breed].push(animal);
    });
    
    // Update statistics
    this.stats.totalAnimals = animals.length;
    this.stats.uniqueBreeds = Object.keys(this.index).length;
    this.stats.lastBuildTime = new Date();
    this.stats.buildDuration = Date.now() - startTime;
    
    return this.stats;
  }

  /**
   * Get all animals of a specific breed
   * @param {String} breed - Breed name (case-insensitive)
   * @returns {Array} Array of animal objects
   * 
   */
  getByBreed(breed) {
    if (!breed) {
      return [];
    }
    
    const normalizedBreed = breed.toLowerCase().trim();
    return this.index[normalizedBreed] || [];
  }

  /**
   * Get animals matching any of multiple breeds
   * @param {Array} breeds - Array of breed names
   * @returns {Array} Combined array of animal objects
   * 
   */
  getByBreeds(breeds) {
    if (!Array.isArray(breeds) || breeds.length === 0) {
      return [];
    }
    
    const results = [];
    breeds.forEach(breed => {
      const animals = this.getByBreed(breed);
      results.push(...animals);
    });
    
    return results;
  }

  /**
   * Get animals matching a partial breed name (contains search)
   * @param {String} partialBreed - Partial breed name to search for
   * @returns {Array} Array of animal objects
   * 
   */
  searchByPartialBreed(partialBreed) {
    if (!partialBreed) {
      return [];
    }
    
    const searchTerm = partialBreed.toLowerCase().trim();
    const results = [];
    
    // Search through breed keys (typically only 50-100 breeds)
    Object.keys(this.index).forEach(breed => {
      if (breed.includes(searchTerm)) {
        results.push(...this.index[breed]);
      }
    });
    
    return results;
  }

  /**
   * Add a new animal to the index
   * @param {Object} animal - Animal object to add
   * 
   */
  addAnimal(animal) {
    if (!animal || !animal.breed) {
      return;
    }
    
    const breed = animal.breed.toLowerCase().trim();
    
    if (!this.index[breed]) {
      this.index[breed] = [];
      this.stats.uniqueBreeds++;
    }
    
    this.index[breed].push(animal);
    this.stats.totalAnimals++;
  }

  /**
   * Remove an animal from the index
   * @param {String} animalId - Animal ID to remove
   * @returns {Boolean} True if animal was found and removed
   * 
   */
  removeAnimal(animalId) {
    for (const breed in this.index) {
      const animals = this.index[breed];
      const index = animals.findIndex(a => 
        a._id.toString() === animalId || a.animal_id === animalId
      );
      
      if (index !== -1) {
        animals.splice(index, 1);
        this.stats.totalAnimals--;
        
        // Remove breed if empty
        if (animals.length === 0) {
          delete this.index[breed];
          this.stats.uniqueBreeds--;
        }
        
        return true;
      }
    }
    
    return false;
  }

  /**
   * Update an animal in the index (e.g., if breed changes)
   * @param {String} animalId - Animal ID to update
   * @param {Object} updatedAnimal - Updated animal object
   * @returns {Boolean} True if animal was found and updated
   */
  updateAnimal(animalId, updatedAnimal) {
    // Remove old entry
    const removed = this.removeAnimal(animalId);
    
    // Add new entry if removal was successful
    if (removed && updatedAnimal) {
      this.addAnimal(updatedAnimal);
      return true;
    }
    
    return false;
  }

  /**
   * Get all unique breed names
   * @returns {Array} Sorted array of breed names
   */
  getAllBreeds() {
    return Object.keys(this.index).sort();
  }

  /**
   * Get breed count statistics
   * @returns {Array} Array of {breed, count} objects sorted by count descending
   */
  getBreedCounts() {
    return Object.entries(this.index)
      .map(([breed, animals]) => ({
        breed,
        count: animals.length
      }))
      .sort((a, b) => b.count - a.count);
  }

  /**
   * Get index statistics
   * @returns {Object} Statistics object
   */
  getStats() {
    return {
      ...this.stats,
      averageAnimalsPerBreed: this.stats.uniqueBreeds > 0 
        ? (this.stats.totalAnimals / this.stats.uniqueBreeds).toFixed(2)
        : 0
    };
  }

  /**
   * Clear the entire index
   */
  clear() {
    this.index = {};
    this.stats = {
      totalAnimals: 0,
      uniqueBreeds: 0,
      lastBuildTime: null,
      buildDuration: 0
    };
  }

  /**
   * Check if index is empty
   * @returns {Boolean}
   */
  isEmpty() {
    return Object.keys(this.index).length === 0;
  }
}

module.exports = BreedHashTable;
