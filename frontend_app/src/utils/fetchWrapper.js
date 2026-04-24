// src/api/fetchWrapper.js

// We'll export a function that is configured with the Auth Context actions
let configuredLogout;
let configuredNavigate;

export const configureFetchWrapper = (logout, navigate) => {
    configuredLogout = logout;
    configuredNavigate = navigate;
};

/*
Uses relative /api/... base URL which works in both scenarios:
In Docker: requests go directly to Traefik on same origin
Local dev: requests go through Vite's proxy to Traefik
*/

/**
 * A wrapper around the native fetch API to handle JWT and 401 errors.
 * @param {string} url - The path (e.g., /devices/get).
 * @param {Object} options - Standard fetch options.
 * @returns {Promise<Object>} - The JSON response data.
 */
export const fetchWrapper = async (url, options = {}) => {
    const token = localStorage.getItem('jwtToken');
    const headers = options.headers || {};

    if (token) {
        // REQUIRED: Add the JWT to the Authorization header
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(url, { ...options, headers });

        if (response.status === 401) {
            // REQUIRED: If 401, token is missing/expired -> redirect to login.
            if (configuredLogout && configuredNavigate) {
                console.warn('Received 401 Unauthorized. Redirecting to login.');
                configuredLogout(); // Clear local storage state
                configuredNavigate('/login', { replace: true });
            } else {
                console.error("Fetch wrapper not configured with logout/navigate functions.");
            }
            // Throw a specific error to halt further processing
            throw new Error('Unauthorized or expired token.');
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `API call failed with status ${response.status}`);
        }

        // Handle cases where the response might be 204 No Content
        const text = await response.text();
        return text ? JSON.parse(text) : null;

    } catch (error) {
        // Do not log the 401 error if it was handled above
        if (error.message !== 'Unauthorized or expired token.') {
            console.error('Error in fetchWrapper:', error);
        }
        throw error;
    }
};