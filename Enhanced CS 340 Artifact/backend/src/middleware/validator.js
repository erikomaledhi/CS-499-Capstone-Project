const { body, param, query, validationResult } = require('express-validator');
const { ValidationError } = require('../utils/errors');

/**
 * Handle validation results
 */
const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    const errorMessages = errors.array().map((error) => error.msg);
    throw new ValidationError(errorMessages.join(', '));
  }
  next();
};

/**
 * Validation rules for user registration
 */
const validateRegister = [
  body('username')
    .trim()
    .isLength({ min: 3, max: 50 })
    .withMessage('Username must be between 3 and 50 characters')
    .matches(/^[a-zA-Z0-9_]+$/)
    .withMessage('Username can only contain letters, numbers, and underscores'),
  body('email')
    .trim()
    .isEmail()
    .withMessage('Please provide a valid email address')
    .normalizeEmail(),
  body('password')
    .isLength({ min: 8 })
    .withMessage('Password must be at least 8 characters long')
    .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
    .withMessage('Password must contain at least one uppercase letter, one lowercase letter, and one number'),
  handleValidationErrors
];

/**
 * Validation rules for user login
 */
const validateLogin = [
  body('username')
    .trim()
    .notEmpty()
    .withMessage('Username is required'),
  body('password')
    .notEmpty()
    .withMessage('Password is required'),
  handleValidationErrors
];

/**
 * Validation rules for animal creation
 */
const validateAnimal = [
  body('animal_id')
    .trim()
    .notEmpty()
    .withMessage('Animal ID is required'),
  body('animal_type')
    .isIn(['Dog', 'Cat', 'Other'])
    .withMessage('Animal type must be Dog, Cat, or Other'),
  body('breed')
    .trim()
    .notEmpty()
    .withMessage('Breed is required'),
  body('age_upon_outcome_in_weeks')
    .isInt({ min: 0 })
    .withMessage('Age must be a positive number'),
  body('sex_upon_outcome')
    .isIn(['Intact Male', 'Intact Female', 'Neutered Male', 'Spayed Female', 'Unknown'])
    .withMessage('Invalid sex_upon_outcome value'),
  body('location_lat')
    .isFloat({ min: -90, max: 90 })
    .withMessage('Latitude must be between -90 and 90'),
  body('location_long')
    .isFloat({ min: -180, max: 180 })
    .withMessage('Longitude must be between -180 and 180'),
  body('datetime')
    .isISO8601()
    .withMessage('Datetime must be a valid ISO 8601 date'),
  handleValidationErrors
];

/**
 * Validation rules for MongoDB ObjectId
 */
const validateObjectId = [
  param('id')
    .isMongoId()
    .withMessage('Invalid ID format'),
  handleValidationErrors
];

/**
 * Validation rules for animal query parameters
 */
const validateAnimalQuery = [
  query('page')
    .optional()
    .isInt({ min: 1 })
    .withMessage('Page must be a positive integer'),
  query('limit')
    .optional()
    .isInt({ min: 1, max: 100 })
    .withMessage('Limit must be between 1 and 100'),
  query('animal_type')
    .optional()
    .isIn(['Dog', 'Cat', 'Other'])
    .withMessage('Invalid animal type'),
  handleValidationErrors
];

module.exports = {
  validateRegister,
  validateLogin,
  validateAnimal,
  validateObjectId,
  validateAnimalQuery,
  handleValidationErrors
};
