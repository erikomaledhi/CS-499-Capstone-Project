const express = require('express');
const {
  register,
  login,
  getMe,
  updatePassword,
  logout
} = require('../controllers/authController');
const { protect } = require('../middleware/auth');
const { validateRegister, validateLogin } = require('../middleware/validator');

const router = express.Router();

// Public routes
router.post('/register', validateRegister, register);
router.post('/login', validateLogin, login);

// Protected routes
router.get('/me', protect, getMe);
router.put('/password', protect, updatePassword);
router.post('/logout', protect, logout);

module.exports = router;
