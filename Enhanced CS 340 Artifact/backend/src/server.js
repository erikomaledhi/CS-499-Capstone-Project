require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const compression = require('compression');
const rateLimit = require('express-rate-limit');
const connectDB = require('./config/database');
const { errorHandler, notFound } = require('./middleware/errorHandler');
const authRoutes = require('./routes/authRoutes');
const animalRoutes = require('./routes/animalRoutes');
const animalCacheService = require('./services/animalCacheService');

// Initialize app
const app = express();

// Connect to database and initialize cache
const initializeServer = async () => {
  // Connect to database first
  await connectDB();
  
  // Initialize cache service after database connection
  try {
    console.log('Initializing cache service...');
    await animalCacheService.initialize();
    console.log('Cache service initialized successfully');
  } catch (error) {
    console.error('Failed to initialize cache service:', error);
    console.log('Server will continue but breed lookups may be slower');
  }
};

// Start initialization
initializeServer();

// Security middleware
app.use(helmet());

// CORS configuration
app.use(
  cors({
    origin: process.env.CORS_ORIGIN || 'http://localhost:3000',
    credentials: true
  })
);

// Rate limiting
const limiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 15 * 60 * 1000, // 15 minutes
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 100,
  message: 'Too many requests from this IP, please try again later'
});
app.use('/api/', limiter);

// Body parser middleware
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Compression middleware
app.use(compression());

// Logging middleware
if (process.env.NODE_ENV === 'development') {
  app.use(morgan('dev'));
} else {
  app.use(morgan('combined'));
}

// Health check route
app.get('/health', (req, res) => {
  res.status(200).json({
    success: true,
    message: 'Server is running',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV
  });
});

// API routes
app.use('/api/auth', authRoutes);
app.use('/api/animals', animalRoutes);

// Welcome route
app.get('/', (req, res) => {
  res.json({
    success: true,
    message: 'Welcome to Grazioso Salvare API',
    version: '1.0.0',
    documentation: '/api/docs',
    endpoints: {
      auth: '/api/auth',
      animals: '/api/animals'
    }
  });
});

// Handle 404
app.use(notFound);

// Error handler (must be last)
app.use(errorHandler);

// Start server
const PORT = process.env.PORT || 5000;
const server = app.listen(PORT, () => {
  console.log(`

    Grazioso Salvare API Server 

    Environment: ${process.env.NODE_ENV || 'development'}${' '.repeat(43 - (process.env.NODE_ENV || 'development').length)}║
    Port: ${PORT}${' '.repeat(50 - PORT.toString().length)}
    URL: http://localhost:${PORT}${' '.repeat(35 - PORT.toString().length)}
                                                         ║
    API Documentation: http://localhost:${PORT}/api/docs${' '.repeat(14 - PORT.toString().length)}
    Health Check: http://localhost:${PORT}/health${' '.repeat(18 - PORT.toString().length)}

  `);
});

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  console.error(`Unhandled Rejection: ${err.message}`);
  server.close(() => process.exit(1));
});

// Handle uncaught exceptions
process.on('uncaughtException', (err) => {
  console.error(`Uncaught Exception: ${err.message}`);
  process.exit(1);
});

module.exports = app;
