/**
 * NameHashTable - High-performance name lookup using hash table
 * 
 */

class NameHashTable {
  constructor() {
    // Hash table: key = animal name (lowercase), value = array of animal objects
    this.index = {};
    
    // Track statistics
    this.stats = {
      totalAnimals: 0,
      uniqueNames: 0,
      lastBuildTime: null,
      buildDuration: 0
    };
  }

  /**
   * Build the name index from an array of animals
   * @param {Array} animals - Array of animal objects from MongoDB
   * @returns {Object} Index statistics
   */
  buildIndex(animals) {
    const startTime = Date.now();
    
    // Clear existing index
    this.index = {};
    
    // Build new index
    animals.forEach(animal => {
      const name = animal.name ? animal.name.toLowerCase().trim() : 'unnamed';
      
      if (!this.index[name]) {
        this.index[name] = [];
      }
      
      this.index[name].push(animal);
    });
    
    // Update statistics
    this.stats.totalAnimals = animals.length;
    this.stats.uniqueNames = Object.keys(this.index).length;
    this.stats.lastBuildTime = new Date();
    this.stats.buildDuration = Date.now() - startTime;
    
    return this.stats;
  }

  /**
   * Get all animals with exact name match
   * @param {String} name - Animal name (case-insensitive)
   * @returns {Array} Array of animal objects
   */
  getByName(name) {
    if (!name) {
      return [];
    }
    
    const normalizedName = name.toLowerCase().trim();
    return this.index[normalizedName] || [];
  }

  /**
   * Search animals by partial name (contains search)
   * @param {String} partialName - Partial name to search for
   * @returns {Array} Array of animal objects
   */
  searchByPartialName(partialName) {
    if (!partialName) {
      return [];
    }
    
    const searchTerm = partialName.toLowerCase().trim();
    const results = [];
    
    // Search through name keys
    Object.keys(this.index).forEach(name => {
      if (name.includes(searchTerm)) {
        results.push(...this.index[name]);
      }
    });
    
    return results;
  }

  /**
   * Add a new animal to the index
   * @param {Object} animal - Animal object to add
   */
  addAnimal(animal) {
    if (!animal) {
      return;
    }
    
    const name = animal.name ? animal.name.toLowerCase().trim() : 'unnamed';
    
    if (!this.index[name]) {
      this.index[name] = [];
      this.stats.uniqueNames++;
    }
    
    this.index[name].push(animal);
    this.stats.totalAnimals++;
  }

  /**
   * Remove an animal from the index
   * @param {String} animalId - Animal ID to remove
   * @returns {Boolean} True if animal was found and removed
   */
  removeAnimal(animalId) {
    for (const name in this.index) {
      const animals = this.index[name];
      const index = animals.findIndex(a => 
        a._id.toString() === animalId || a.animal_id === animalId
      );
      
      if (index !== -1) {
        animals.splice(index, 1);
        this.stats.totalAnimals--;
        
        if (animals.length === 0) {
          delete this.index[name];
          this.stats.uniqueNames--;
        }
        
        return true;
      }
    }
    
    return false;
  }

  /**
   * Update an animal in the index
   * @param {String} animalId - Animal ID to update
   * @param {Object} updatedAnimal - Updated animal object
   * @returns {Boolean} True if animal was found and updated
   */
  updateAnimal(animalId, updatedAnimal) {
    const removed = this.removeAnimal(animalId);
    
    if (removed && updatedAnimal) {
      this.addAnimal(updatedAnimal);
      return true;
    }
    
    return false;
  }

  /**
   * Get index statistics
   * @returns {Object} Statistics object
   */
  getStats() {
    return {
      ...this.stats,
      averageAnimalsPerName: this.stats.uniqueNames > 0 
        ? (this.stats.totalAnimals / this.stats.uniqueNames).toFixed(2)
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
      uniqueNames: 0,
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

module.exports = NameHashTable;
