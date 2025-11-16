import api from './api';

/**
 * Authentication Service
 */
const authService = {
  /**
   * Register new user
   */
  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    if (response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.data.user));
    }
    return response.data;
  },

  /**
   * Login user
   */
  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    if (response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.data.user));
    }
    return response.data;
  },

  /**
   * Logout user
   */
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  /**
   * Get current user
   */
  getMe: async () => {
    const response = await api.get('/auth/me');
    return response.data;
  },

  /**
   * Update password
   */
  updatePassword: async (passwords) => {
    const response = await api.put('/auth/password', passwords);
    if (response.data.data.token) {
      localStorage.setItem('token', response.data.data.token);
    }
    return response.data;
  },

  /**
   * Get current user from localStorage
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },

  /**
   * Check if user is admin
   */
  isAdmin: () => {
    const user = authService.getCurrentUser();
    return user && user.role === 'admin';
  }
};

export default authService;
