const { AuthenticationError } = require('../utils/errors');
const { verifyToken } = require('../utils/jwt');
const User = require('../models/User');

/**
 * Protect routes - require valid JWT token
 */
const protect = async (req, res, next) => {
  try {
    let token;

    // Check for token in Authorization header
    if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
      token = req.headers.authorization.split(' ')[1];
    }

    // Check if token exists
    if (!token) {
      throw new AuthenticationError('Not authorized to access this route. Please login.');
    }

    try {
      // Verify token
      const decoded = verifyToken(token);

      // Get user from token and attach to request
      req.user = await User.findById(decoded.id).select('-password');

      if (!req.user) {
        throw new AuthenticationError('User no longer exists');
      }

      if (!req.user.isActive) {
        throw new AuthenticationError('User account is deactivated');
      }

      next();
    } catch (error) {
      if (error.name === 'JsonWebTokenError') {
        throw new AuthenticationError('Invalid token');
      }
      if (error.name === 'TokenExpiredError') {
        throw new AuthenticationError('Token has expired. Please login again.');
      }
      throw error;
    }
  } catch (error) {
    next(error);
  }
};

/**
 * Restrict to specific roles
 * @param  {...string} roles - Allowed roles
 */
const restrictTo = (...roles) => {
  return (req, res, next) => {
    if (!roles.includes(req.user.role)) {
      return next(
        new AuthenticationError(
          `User role '${req.user.role}' is not authorized to access this route`
        )
      );
    }
    next();
  };
};

module.exports = { protect, restrictTo };
