import api from './api';

/**
 * Animal Service
 */
const animalService = {
  /**
   * Get all animals with filters
   */
  getAnimals: async (params = {}) => {
    const response = await api.get('/animals', { params });
    return response.data;
  },

  /**
   * Get single animal by ID
   */
  getAnimalById: async (id) => {
    const response = await api.get(`/animals/${id}`);
    return response.data;
  },

  /**
   * Get animal by animal_id
   */
  getAnimalByAnimalId: async (animalId) => {
    const response = await api.get(`/animals/animal-id/${animalId}`);
    return response.data;
  },

  /**
   * Update animal
   */
  updateAnimal: async (id, animalData) => {
    const response = await api.put(`/animals/${id}`, animalData);
    return response.data;
  },

  /**
   * Delete animal
   */
  deleteAnimal: async (id) => {
    const response = await api.delete(`/animals/${id}`);
    return response.data;
  },

  /**
   * Get water rescue dogs
   */
  getWaterRescueDogs: async () => {
    const response = await api.get('/animals/rescue/water');
    return response.data;
  },

  /**
   * Get mountain rescue dogs
   */
  getMountainRescueDogs: async () => {
    const response = await api.get('/animals/rescue/mountain');
    return response.data;
  },

  /**
   * Get disaster rescue dogs
   */
  getDisasterRescueDogs: async () => {
    const response = await api.get('/animals/rescue/disaster');
    return response.data;
  },

  /**
   * Get breed statistics
   */
  getBreedStats: async () => {
    const response = await api.get('/animals/stats/breeds');
    return response.data;
  },

  // ============================================
  // OPTIMIZED HASH TABLE ENDPOINTS (Enhancement 2)
  // ============================================

  /**
   * Get breed counts/popularity (FAST - from hash table)
   * Use this for charts, maps, and breed distribution visualization
   */
  getBreedCounts: async (limit = 20) => {
    const response = await api.get(`/animals/stats/breed-counts?limit=${limit}`);
    return response.data;
  },

  /**
   * Get cache statistics
   * Shows hash table performance metrics
   */
  getCacheStats: async () => {
    const response = await api.get('/animals/cache/stats');
    return response.data;
  },

  /**
   * Search animals by name (FAST - hash table lookup)
   */
  searchAnimalsByName: async (searchTerm) => {
    const response = await api.get(`/animals/search-name?q=${encodeURIComponent(searchTerm)}`);
    return response.data;
  }
};

export default animalService;
