const express = require('express');
const {
  getAnimals,
  getAnimalById,
  getAnimalByAnimalId,
  createAnimal,
  updateAnimal,
  deleteAnimal,
  getWaterRescueDogs,
  getMountainRescueDogs,
  getDisasterRescueDogs,
  getBreedStats
} = require('../controllers/animalController');
const { protect, restrictTo } = require('../middleware/auth');
const {
  validateAnimal,
  validateObjectId,
  validateAnimalQuery
} = require('../middleware/validator');

const router = express.Router();

// Public routes
router.get('/', validateAnimalQuery, getAnimals);
router.get('/stats/breeds', getBreedStats);
router.get('/rescue/water', getWaterRescueDogs);
router.get('/rescue/mountain', getMountainRescueDogs);
router.get('/rescue/disaster', getDisasterRescueDogs);
router.get('/animal-id/:animal_id', getAnimalByAnimalId);
router.get('/:id', validateObjectId, getAnimalById);

// Protected routes (Admin only)
router.put('/:id', protect, restrictTo('admin'), validateObjectId, updateAnimal);
router.delete('/:id', protect, restrictTo('admin'), validateObjectId, deleteAnimal);

module.exports = router;
