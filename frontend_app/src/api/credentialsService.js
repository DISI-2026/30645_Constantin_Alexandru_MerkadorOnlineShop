// src/api/credentialsService.js

// fetchWrapper must be imported to handle JWT injection and 401 redirection
import { fetchWrapper } from '../utils/fetchWrapper';

const BASE_URL = '/api/credentials';

/**
 * Handles the login request to the backend.
 * @param {string} username - The user's username.
 * @param {string} password - The user's password.
 * @returns {Promise<Object>} - The user data, including the JWT and role.
 */
export const login = async (username, password) => {
    try {
        const response = await fetch(`${BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
        });

        if (!response.ok) {
            // Handle HTTP errors (e.g., 401 Unauthorized for bad credentials)
            const errorData = await response.json();
            throw new Error(errorData.message || 'Login failed due to server error.');
        }

        const data = await response.json();
        return data;

    } catch (error) {
        console.error('Error during login API call:', error);
        throw error;
    }
};

export const getCredentialsById = async (userId) => {
    // Calls: /api/credentials
    return fetchWrapper(`${BASE_URL}/${userId}`, { method: 'GET' });
};

export const getAllCredentials = async () => {
    return fetchWrapper(BASE_URL, { method: 'GET' });
}