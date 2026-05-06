// src/api/userService.js
import { fetchWrapper } from '../utils/fetchWrapper';

const BASE_URL = '/api/users';

// ==========================================
// PROFILE ENDPOINTS
// ==========================================

export const getAllUsers = async () => {
    return fetchWrapper(BASE_URL, { method: 'GET' });
};

export const getUserById = async (id) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}`, { method: 'GET' });
};

export const updateUser = async (id, updateData) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateData),
    });
};

export const uploadAvatar = async (id, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/avatar`, {
        method: 'POST',
        body: formData,
    });
};

// ==========================================
// SELLER ENDPOINTS
// ==========================================

export const getSellerProfile = async (id) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/seller-profile`, { method: 'GET' });
};

export const createSellerProfile = async (id, profileData) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/seller-profile`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(profileData),
    });
};

export const updateSellerProfile = async (id, profileData) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/seller-profile/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(profileData),
    });
};

export const getPendingSellers = async () => {
    return fetchWrapper(`${BASE_URL}/sellers/pending`, { method: 'GET' });
};

// Admins call this endpoint to verify a seller's profile
export const verifySellerProfile = async (id, authorizedCategories) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/seller-profile/verify`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ authorizedCategories }),
    });
};

// ==========================================
// ADDRESS BOOK ROUTES
// ==========================================

export const getUserAddresses = async (id) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/addresses`, { method: 'GET' });
};

export const getDefaultUserAddress = async (id) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/default_address`, { method: 'GET' });
};

export const addAddress = async (id, addressData) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/addresses`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(addressData),
    });
};

export const setDefaultAddress = async (id, addressId) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/addresses/${encodeURIComponent(addressId)}/default`, {
        method: 'PUT',
    });
};

export const deleteAddress = async (id, addressId) => {
    return fetchWrapper(`${BASE_URL}/${encodeURIComponent(id)}/addresses/${encodeURIComponent(addressId)}`, {
        method: 'DELETE',
    });
};