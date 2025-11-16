const User = require('../models/User');
const { generateToken } = require('../utils/jwt');
const ApiResponse = require('../utils/apiResponse');
const { AuthenticationError, ConflictError, ValidationError } = require('../utils/errors');

/**
 * @desc    Register new user
 * @route   POST /api/auth/register
 * @access  Public
 */
const register = async (req, res, next) => {
  try {
    const { username, email, password, role } = req.body;

    // Check if user already exists
    const userExists = await User.findOne({ $or: [{ username }, { email }] });
    if (userExists) {
      throw new ConflictError('Username or email already exists');
    }

    // Create user
    const user = await User.create({
      username,
      email,
      password,
      role: role || 'user'
    });

    // Generate token
    const token = generateToken(user._id);

    res.status(201).json(
      ApiResponse.created('User registered successfully', {
        user: {
          id: user._id,
          username: user.username,
          email: user.email,
          role: user.role
        },
        token
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Login user
 * @route   POST /api/auth/login
 * @access  Public
 */
const login = async (req, res, next) => {
  try {
    const { username, password } = req.body;

    // Find user and include password
    const user = await User.findOne({ username }).select('+password');

    if (!user) {
      throw new AuthenticationError('Invalid credentials');
    }

    // Check if password matches
    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      throw new AuthenticationError('Invalid credentials');
    }

    // Check if user is active
    if (!user.isActive) {
      throw new AuthenticationError('Account is deactivated');
    }

    // Update last login
    await user.updateLastLogin();

    // Generate token
    const token = generateToken(user._id);

    res.status(200).json(
      ApiResponse.success('Login successful', {
        user: {
          id: user._id,
          username: user.username,
          email: user.email,
          role: user.role,
          lastLogin: user.lastLogin
        },
        token
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Get current logged in user
 * @route   GET /api/auth/me
 * @access  Private
 */
const getMe = async (req, res, next) => {
  try {
    const user = await User.findById(req.user.id);

    res.status(200).json(
      ApiResponse.success('User retrieved successfully', {
        user
      })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Update password
 * @route   PUT /api/auth/password
 * @access  Private
 */
const updatePassword = async (req, res, next) => {
  try {
    const { currentPassword, newPassword } = req.body;

    if (!currentPassword || !newPassword) {
      throw new ValidationError('Please provide current and new password');
    }

    // Get user with password
    const user = await User.findById(req.user.id).select('+password');

    // Check current password
    const isMatch = await user.comparePassword(currentPassword);
    if (!isMatch) {
      throw new AuthenticationError('Current password is incorrect');
    }

    // Update password
    user.password = newPassword;
    await user.save();

    // Generate new token
    const token = generateToken(user._id);

    res.status(200).json(
      ApiResponse.success('Password updated successfully', { token })
    );
  } catch (error) {
    next(error);
  }
};

/**
 * @desc    Logout user
 * @route   POST /api/auth/logout
 * @access  Private
 */
const logout = async (req, res, next) => {
  try {
    // In a stateless JWT system, logout is handled client-side by removing the token
    // This endpoint is optional but can be used for logging/auditing
    res.status(200).json(
      ApiResponse.success('Logout successful')
    );
  } catch (error) {
    next(error);
  }
};

module.exports = {
  register,
  login,
  getMe,
  updatePassword,
  logout
};
