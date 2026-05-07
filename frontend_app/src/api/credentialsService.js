// src/api/credentialsService.js
import { fetchWrapper } from '../utils/fetchWrapper';

const BASE_URL = '/api/credentials';

// ==========================================
// PUBLIC ENDPOINTS
// ==========================================

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

export const login = async (email, password) => {
    const response = await fetch(`${BASE_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
    });
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const error = new Error(errorData.message || 'Login failed. Please try again.');
        error.resource = errorData.resource;
        throw error;
    }
    return response.json();
};

export const activate = async (email, code) => {
    return fetchWrapper(`${BASE_URL}/activate?email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`, {
        method: 'POST',
    });
};

export const resendCode = async (email) => {
    return fetchWrapper(`${BASE_URL}/resend-code?email=${encodeURIComponent(email)}`, {
        method: 'POST',
    });
};

export const forgotPassword = async (email) => {
    return fetchWrapper(`${BASE_URL}/forgot-password?email=${encodeURIComponent(email)}`, {
        method: 'POST',
    });
};

export const resetPassword = async (token, newPassword) => {
    return fetchWrapper(`${BASE_URL}/reset-password?token=${encodeURIComponent(token)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ newPassword }),
    });
};

export const reactivate = async (email) => {
    return fetchWrapper(`${BASE_URL}/reactivate-account?email=${encodeURIComponent(email)}`, {
        method: 'PATCH',
    });
};

// ==========================================
// PRIVATE ENDPOINTS - USER
// ==========================================

export const logout = async (refreshToken) => {
    return fetchWrapper(`${BASE_URL}/logout?refreshToken=${encodeURIComponent(refreshToken)}`, {
        method: 'POST',
    });
};

export const deactivate = async (id) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/deactivate`, {
        method: 'PATCH',
    });
};

export const switchRole = async (id, targetRole) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/switch-role`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ targetRole }),
    });
};

export const changePassword = async (id, oldPassword, newPassword) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/password`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ oldPassword, newPassword }),
    });
};

export const requestEmailUpdate = async (id, newEmail) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/email-update-request?newEmail=${encodeURIComponent(newEmail)}`, {
        method: 'POST',
    });
};

export const confirmEmailUpdate = async (id, code) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/email-update-confirm?code=${encodeURIComponent(code)}`, {
        method: 'POST',
    });
};

// ==========================================
// PRIVATE ENDPOINTS - ADMIN
// ==========================================

export const getAllCredentials = async () => {
    return fetchWrapper(BASE_URL, { method: 'GET' });
};

export const getCredentialById = async (id) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}`, { method: 'GET' });
};

export const getCredentialByEmail = async (email) => {
    return fetchWrapper(`${BASE_URL}/email/${encodeURIComponent(email)}`, { method: 'GET' });
};

export const updateCredentialStatus = async (id, newStatus) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/status?newStatus=${encodeURIComponent(newStatus)}`, {
        method: 'PUT',
    });
};

export const addCredentialRole = async (id, newRole) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/role?newRole=${encodeURIComponent(newRole)}`, {
        method: 'POST',
    });
};

export const deleteCredential = async (id) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/delete`, {
        method: 'DELETE',
    });
};