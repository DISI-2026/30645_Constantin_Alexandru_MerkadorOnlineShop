// src/api/userService.js

import { fetchWrapper } from '../utils/fetchWrapper';

const BASE_URL = '/api/users';

/**
 * Retrieves a list of all users.
 * Matches: GET /users
 */
export const getAllUsers = async () => {
    return fetchWrapper(BASE_URL, { method: 'GET' });
};

/**
 * Retrieves a user by their UUID.
 * Matches: GET /users/{id}
 */
export const getUserById = async (id) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}`, { method: 'GET' });
};

/**
 * Updates an existing user's details.
 * Matches: PUT /users/{id}/update
 * Note: Ensure updateData matches your UserProfileReqDTO (e.g., { firstName: "...", lastName: "..." })
 */
export const updateUser = async (id, updateData) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateData),
    });
};

/**
 * Uploads an avatar image for a user.
 * Matches: POST /users/{id}/avatar
 */
export const uploadAvatar = async (id, file) => {
    const formData = new FormData();
    formData.append('file', file);

    // Note: Do not set 'Content-Type': 'application/json' or 'multipart/form-data'.
    // The browser sets the correct multipart boundary automatically when using FormData.
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/avatar`, {
        method: 'POST',
        body: formData,
    });
};