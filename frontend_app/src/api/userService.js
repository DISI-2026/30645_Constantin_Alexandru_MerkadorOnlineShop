// src/api/userService.js

// fetchWrapper must be imported to handle JWT injection and 401 redirection
import { fetchWrapper } from '../utils/fetchWrapper';

const BASE_URL = '/api/users';

/**
 * Retrieves a list of all users (Protected: requires ADMIN/JWT).
 * @returns {Promise<Array<Object>>} List of user objects.
 */
export const getAllUsers = async () => {
    // Calls: /api/users
    return fetchWrapper(BASE_URL, { method: 'GET' });
};

/**
 * Retrieves a user by their full name (Protected: requires JWT).
 * @param {string} fullName - The full name of the user to search for.
 * @returns {Promise<Object>} The user object.
 */
export const getUserByFullName = async (fullName) => {
    // Calls: /api/users/{fullName}
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(fullName)}`, { method: 'GET' });
};

/**
 * Creates a new user (PUBLIC (no token required): can be used for future registration feature, but currently only admin creates users).
 * @param {Object} userData - User details (fullName, email, username, password, role).
 */
export const createUser = async (userData) => {
    // Calls: /api/users/add
    return fetch(`${BASE_URL}/add`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData),
    });
};

/**
 * Updates an existing user's details.
 * @param {string} id - UUID of the user.
 * @param {Object} updateData - User details including newPassword (optional).
 */
export const updateUser = async (id, updateData) => {
    // Calls: /api/users/{id}/update
    return fetchWrapper(`${BASE_URL}/${id}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateData),
    });
};

/**
 * Deletes a user by ID.
 * @param {string} id - UUID of the user.
 */
export const deleteUser = async (id) => {
    // Calls: /api/users/{id}/delete
    return fetchWrapper(`${BASE_URL}/${id}/delete`, {
        method: 'DELETE',
    });
};



