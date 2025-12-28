import axios from 'axios';

// Base URL for backend API
const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ============================================
// PRICE ENDPOINTS
// ============================================

export const priceApi = {
  // Get current price for a zone
  getCurrentPrice: (zone) => api.get(`/prices/current/${zone}`),

  // Get today's prices for a zone
  getTodaysPrices: (zone) => api.get(`/prices/today/${zone}`),

  // Get tomorrow's prices for a zone
  getTomorrowsPrices: (zone) => api.get(`/prices/tomorrow/${zone}`),

  // Get cheapest hours today
  getCheapestHours: (zone, limit = 3) => api.get(`/prices/cheapest/${zone}?limit=${limit}`),

  // Get statistics for today
  getStatistics: (zone) => api.get(`/prices/statistics/${zone}`),

  // Manually fetch prices
  fetchPrices: () => api.post('/prices/fetch'),
};

// ============================================
// USER ENDPOINTS
// ============================================

export const userApi = {
  // Register new user
  registerUser: (userData) => api.post('/users', userData),

  // Get user by ID
  getUserById: (id) => api.get(`/users/${id}`),

  // Get user by email
  getUserByEmail: (email) => api.get(`/users/email/${email}`),

  // Update user preferences
  updateUser: (id, updates) => api.put(`/users/${id}`, updates),

  // Enable alerts
  enableAlerts: (id) => api.patch(`/users/${id}/alerts/enable`),

  // Disable alerts
  disableAlerts: (id) => api.patch(`/users/${id}/alerts/disable`),
};

// ============================================
// ALERT ENDPOINTS
// ============================================

export const alertApi = {
  // Get alerts for user
  getAlertsForUser: (userId) => api.get(`/alerts/user/${userId}`),

  // Get today's alerts for user
  getTodaysAlerts: (userId) => api.get(`/alerts/user/${userId}/today`),
};

export default api;