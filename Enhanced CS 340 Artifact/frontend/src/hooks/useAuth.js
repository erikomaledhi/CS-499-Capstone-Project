import { useState, useEffect } from 'react';
import authService from '../services/authService';

/**
 * Custom hook for authentication
 */
const useAuth = () => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = () => {
    const currentUser = authService.getCurrentUser();
    const authenticated = authService.isAuthenticated();
    const admin = authService.isAdmin();

    setUser(currentUser);
    setIsAuthenticated(authenticated);
    setIsAdmin(admin);
    setLoading(false);
  };

  const login = async (credentials) => {
    const response = await authService.login(credentials);
    checkAuth();
    return response;
  };

  const register = async (userData) => {
    const response = await authService.register(userData);
    checkAuth();
    return response;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
    setIsAdmin(false);
  };

  return {
    user,
    isAuthenticated,
    isAdmin,
    loading,
    login,
    register,
    logout,
    checkAuth
  };
};

export default useAuth;
