// src/api/apiClient.js
import axios from 'axios';

let configuredLogout = null;
let configuredNavigate = null;
let getRefreshToken = null;
let setTokens = null; // function(accessToken, refreshToken)

export const configureApiClient = (options = {}) => {
  configuredLogout = options.logout || null;
  configuredNavigate = options.navigate || null;
  getRefreshToken = options.getRefreshToken || null;
  setTokens = options.setTokens || null;
};

export const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: false,
});

// Attach Authorization header if we have an access token
apiClient.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem('accessToken');
  if (accessToken) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let isRefreshing = false;
let pendingRequests = [];

const processQueue = (error, token = null) => {
  pendingRequests.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  pendingRequests = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (!originalRequest || error.response?.status !== 401) {
      return Promise.reject(error);
    }

    // Avoid infinite loop
    if (originalRequest._retry) {
      // Already retried once, do a hard logout
      if (configuredLogout && configuredNavigate) {
        configuredLogout();
        configuredNavigate('/login', { replace: true });
      }
      return Promise.reject(error);
    }

    if (!getRefreshToken || !setTokens) {
      // No refresh strategy configured
      if (configuredLogout && configuredNavigate) {
        configuredLogout();
        configuredNavigate('/login', { replace: true });
      }
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingRequests.push({ resolve, reject });
      })
        .then((newAccessToken) => {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return apiClient(originalRequest);
        })
        .catch((err) => Promise.reject(err));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const refreshToken = getRefreshToken();
      if (!refreshToken) throw new Error('Missing refresh token');

      const resp = await axios.post('/api/credentials/refresh', { refreshToken });
      const { accessToken: newAccessToken, refreshToken: newRefreshToken } = resp.data || {};
      if (!newAccessToken || !newRefreshToken) throw new Error('Invalid refresh response');

      // Persist tokens
      setTokens(newAccessToken, newRefreshToken);

      processQueue(null, newAccessToken);
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return apiClient(originalRequest);
    } catch (refreshErr) {
      processQueue(refreshErr, null);
      if (configuredLogout && configuredNavigate) {
        configuredLogout();
        configuredNavigate('/login', { replace: true });
      }
      return Promise.reject(refreshErr);
    } finally {
      isRefreshing = false;
    }
  }
);
