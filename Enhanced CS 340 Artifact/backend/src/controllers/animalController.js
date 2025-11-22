const Animal = require('../models/Animal');
const ApiResponse = require('../utils/apiResponse');
const { NotFoundError, ValidationError } = require('../utils/errors');
const animalCacheService = require('../services/animalCacheService');

/**
 * @desc    Get all animals with pagination and filtering
 * @route   GET /api/animals
 * @access  Public
 */
const getAnimals = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page, 10) || 1;
    const limit = parseInt(req.query.limit, 10) || 10000;
    const skip = (page - 1) * limit;

    // Build query
    const query = {};

    // Filter by animal type
    if (req.query.animal_type) {
      query.animal_type = req.query.animal_type;
    }

    // Filter by breed
    if (req.query.breed) {
      query.breed = new RegExp(req.query.breed, 'i');
    }

    // Filter by sex
    if (req.query.sex_upon_outcome) {
      query.sex_upon_outcome = new RegExp(req.query.sex_upon_outcome, 'i');
    }

    // Filter by age range
    if (req.query.min_age || req.query.max_age) {
      query.age_upon_outcome_in_weeks = {};
      if (req.query.min_age) {
        query.age_upon_outcome_in_weeks.$gte = parseInt(req.query.min_age, 10);
      }
      if (req.query.max_age) {
        query.age_upon_outcome_in_weeks.$lte = parseInt(req.query.max_age, 10);
      }
    }

    // Build sort options
    const sortField = req.query.sortBy || 'datetime';
    const sortOrder = req.query.order === 'asc' ? 1 : -1;
    const sortOptions = { [sortField]: sortOrder };

    // Execute query with pagination
    const animals = await Animal.find(query)
      .limit(limit)
      .skip(skip)
      .sort(sortOptions);

    // Get total count for pagination
    const total = await Animal.countDocuments(query);

    res.status(200).json(
      ApiResponse.success('Animals retrieved successfully', {
        animals,
        pagination: {
          page,
          limit,
          total,
          pages: Math.ceil(total / limit)
        }
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Get single animal by ID
 * @route   GET /api/animals/:id
 * @access  Public
 */
const getAnimalById = async (req, res, next) => {
  try {
    const animal = await Animal.findById(req.params.id);

    if (!animal) {
      throw new NotFoundError('Animal');
    }

    res.status(200).json(
      ApiResponse.success('Animal retrieved successfully', { animal })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Get animal by animal_id using BST (O(log n) lookup)
 * @route   GET /api/animals/animal-id/:animal_id
 * @access  Public
 * 
 */
const getAnimalByAnimalId = async (req, res, next) => {
  try {
    // Try BST lookup first (O(log n))
    const result = animalCacheService.searchById(req.params.animal_id);

    if (result && result.animal) {
      return res.status(200).json(
        ApiResponse.success('Animal retrieved successfully', {
          animal: result.animal,
          comparisons: result.comparisons,
          source: 'bst-cache'
        })
      );
    }

    // Fallback to database if not in cache
    const animal = await Animal.findOne({ animal_id: req.params.animal_id });

    if (!animal) {
      throw new NotFoundError('Animal');
    }

    res.status(200).json(
      ApiResponse.success('Animal retrieved successfully', {
        animal,
        source: 'database'
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Create new animal
 * @route   POST /api/animals
 * @access  Private (Admin only)
 */
const createAnimal = async (req, res, next) => {
  try {
    const animal = await Animal.create(req.body);

    // Update cache with new animal
    animalCacheService.onAnimalCreated(animal);

    res.status(201).json(
      ApiResponse.created('Animal created successfully', { animal })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Update animal
 * @route   PUT /api/animals/:id
 * @access  Private (Admin only)
 */
const updateAnimal = async (req, res, next) => {
  try {
    const animal = await Animal.findByIdAndUpdate(
      req.params.id,
      req.body,
      {
        new: true,
        runValidators: true
      }
    );

    if (!animal) {
      throw new NotFoundError('Animal');
    }

    // Update cache with modified animal
    animalCacheService.onAnimalUpdated(req.params.id, animal);

    res.status(200).json(
      ApiResponse.success('Animal updated successfully', { animal })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Delete animal
 * @route   DELETE /api/animals/:id
 * @access  Private (Admin only)
 */
const deleteAnimal = async (req, res, next) => {
  try {
    const animal = await Animal.findByIdAndDelete(req.params.id);

    if (!animal) {
      throw new NotFoundError('Animal');
    }

    // Remove from cache
    animalCacheService.onAnimalDeleted(req.params.id);

    res.status(200).json(
      ApiResponse.success('Animal deleted successfully')
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Get water rescue dogs
 * @route   GET /api/animals/rescue/water
 * @access  Public
 */
const getWaterRescueDogs = async (req, res, next) => {
  try {
    const dogs = await Animal.getWaterRescueDogs();

    res.status(200).json(
      ApiResponse.success('Water rescue dogs retrieved successfully', {
        animals: dogs,
        count: dogs.length
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Get mountain rescue dogs
 * @route   GET /api/animals/rescue/mountain
 * @access  Public
 */
const getMountainRescueDogs = async (req, res, next) => {
  try {
    const dogs = await Animal.getMountainRescueDogs();

    res.status(200).json(
      ApiResponse.success('Mountain rescue dogs retrieved successfully', {
        animals: dogs,
        count: dogs.length
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Get disaster rescue dogs
 * @route   GET /api/animals/rescue/disaster
 * @access  Public
 */
const getDisasterRescueDogs = async (req, res, next) => {
  try {
    const dogs = await Animal.getDisasterRescueDogs();

    res.status(200).json(
      ApiResponse.success('Disaster rescue dogs retrieved successfully', {
        animals: dogs,
        count: dogs.length
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Get breed statistics
 * @route   GET /api/animals/stats/breeds
 * @access  Public
 */
const getBreedStats = async (req, res, next) => {
  try {
    const stats = await Animal.aggregate([
      {
        $group: {
          _id: '$breed',
          count: { $sum: 1 }
        }
      },
      {
        $sort: { count: -1 }
      },
      {
        $limit: 20
      }
    ]);

    res.status(200).json(
      ApiResponse.success('Breed statistics retrieved successfully', { stats })
    );
  } catch (error) {
    next(error);
  }
};



/**
 * @desc    Get breed counts (sorted by popularity)
 * @route   GET /api/animals/stats/breed-counts
 * @access  Public
 */
const getBreedCountsOptimized = async (req, res, next) => {
  try {
    const limit = parseInt(req.query.limit, 10) || 20;
    
    // Get breed counts from hash table (much faster than aggregation)
    const breedCounts = animalCacheService.getBreedCounts().slice(0, limit);

    res.status(200).json(
      ApiResponse.success('Breed counts retrieved successfully', {
        breedCounts,
        count: breedCounts.length,
        source: 'cache'
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Get cache statistics
 * @route   GET /api/animals/cache/stats
 * @access  Public
 */
const getCacheStats = async (req, res, next) => {
  try {
    const stats = animalCacheService.getStats();

    res.status(200).json(
      ApiResponse.success('Cache statistics retrieved successfully', { stats })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Rebuild cache (admin only)
 * @route   POST /api/animals/cache/rebuild
 * @access  Private (Admin only)
 */
const rebuildCache = async (req, res, next) => {
  try {
    const result = await animalCacheService.rebuild();

    res.status(200).json(
      ApiResponse.success('Cache rebuilt successfully', { result })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Search animals by name using optimized hash table lookup
 * @route   GET /api/animals/search-name
 * @access  Public
 * 
 */
const searchAnimalsByName = async (req, res, next) => {
  try {
    const { q } = req.query;
    
    if (!q) {
      throw new ValidationError('Search query parameter "q" is required');
    }

    // Use hash table for optimized name search
    const animals = animalCacheService.searchByPartialName(q);

    res.status(200).json(
      ApiResponse.success(`Animals matching name search '${q}' retrieved successfully`, {
        animals,
        count: animals.length,
        searchTerm: q,
        source: 'cache'
      })
    );
  } catch (error) {
    next(error);
  }
};

module.exports = {
  getAnimals,
  getAnimalById,
  getAnimalByAnimalId,
  createAnimal,
  updateAnimal,
  deleteAnimal,
  getWaterRescueDogs,
  getMountainRescueDogs,
  getDisasterRescueDogs,
  getBreedStats,
  getBreedCountsOptimized,
  getCacheStats,
  rebuildCache,
  searchAnimalsByName
};
