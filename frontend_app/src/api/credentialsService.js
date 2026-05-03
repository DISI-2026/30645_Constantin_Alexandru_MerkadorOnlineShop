// src/api/credentialsService.js

import { fetchWrapper } from '../utils/fetchWrapper';

const BASE_URL = '/api/credentials';

// Auth Endpoints
// Login: POST /credentials/login { email, password }
export const login = async (email, password) => {
    const response = await fetch(`${BASE_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        // Throw the raw object so we can read errorData.resource in the UI
        throw new Error(errorData.message || 'Login failed. Please try again.');
    }

    return response.json();
};

// Register: POST /credentials/add { email, password, firstName, lastName }
export const register = async ({ email, password, firstName, lastName }) => {
  const response = await fetch(`${BASE_URL}/add`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, firstName, lastName }),
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Registration failed.');
  }
  return response.json().catch(() => ({}));
};

// Activate Account: POST /credentials/activate?email=E&code=C
export const activate = async (email, code) => {
  return fetchWrapper(`${BASE_URL}/activate?email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`, {
    method: 'POST',
  });
};

// Resend Code: POST /credentials/resend-code?email=E
export const resendCode = async (email) => {
  return fetchWrapper(`${BASE_URL}/resend-code?email=${encodeURIComponent(email)}`, {
    method: 'POST',
  });
};

// Forgot Password: POST /credentials/forgot-password?email=E
export const forgotPassword = async (email) => {
  return fetchWrapper(`${BASE_URL}/forgot-password?email=${encodeURIComponent(email)}`, {
    method: 'POST',
  });
};

// Reset Password: POST /credentials/reset-password?token=T  body: { newPassword }
export const resetPassword = async (token, newPassword) => {
  return fetchWrapper(`${BASE_URL}/reset-password?token=${encodeURIComponent(token)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ newPassword }),
  });
};

// Logout: POST /credentials/logout?refreshToken=...
export const logout = async (refreshToken) => {
  return fetchWrapper(`${BASE_URL}/logout?refreshToken=${encodeURIComponent(refreshToken)}`, {
    method: 'POST',
  });
};