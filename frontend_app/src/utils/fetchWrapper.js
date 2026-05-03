// src/api/fetchWrapper.js

// We'll export a function that is configured with the Auth Context actions
let configuredLogout;
let configuredNavigate;
let configuredGetRefreshToken;
let configuredSetTokens;
let isRefreshing = false;
let pendingQueue = [];

export const configureFetchWrapper = (logout, navigate, getRefreshToken, setTokens) => {
    configuredLogout = logout;
    configuredNavigate = navigate;
    configuredGetRefreshToken = getRefreshToken;
    configuredSetTokens = setTokens;
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
    const accessToken = localStorage.getItem('accessToken');
    const headers = { ...(options.headers || {}) };

    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const doFetch = async () => {
        return fetch(url, { ...options, headers });
    };

    try {
        let response = await doFetch();

        if (response.status === 401) {
            // Attempt refresh flow if configured
            if (configuredGetRefreshToken && configuredSetTokens) {
                if (isRefreshing) {
                    // Queue the request until refresh completes
                    return new Promise((resolve, reject) => {
                        pendingQueue.push({ resolve, reject, url, options });
                    });
                }

                isRefreshing = true;
                try {
                    const refreshToken = configuredGetRefreshToken();
                    if (!refreshToken) throw new Error('Missing refresh token');

                    const refreshResp = await fetch('/api/credentials/refresh', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ refreshToken })
                    });

                    if (!refreshResp.ok) throw new Error('Refresh failed');

                    const { accessToken: newAccessToken, refreshToken: newRefreshToken } = await refreshResp.json();
                    if (!newAccessToken || !newRefreshToken) throw new Error('Invalid refresh response');

                    // Persist via context-provided setter and localStorage for header injection
                    configuredSetTokens(newAccessToken, newRefreshToken);

                    // Retry original request with new token
                    headers['Authorization'] = `Bearer ${newAccessToken}`;
                    response = await doFetch();

                    // Release queued requests
                    pendingQueue.forEach(async (pending) => {
                        try {
                            const retryHeaders = { ...(pending.options.headers || {}) };
                            retryHeaders['Authorization'] = `Bearer ${newAccessToken}`;
                            const retryResp = await fetch(pending.url, { ...pending.options, headers: retryHeaders });
                            if (!retryResp.ok) {
                                const errData = await retryResp.json().catch(() => ({}));
                                pending.reject(new Error(errData.message || `API call failed with status ${retryResp.status}`));
                            } else {
                                const text = await retryResp.text();
                                const data = text ? JSON.parse(text) : null;
                                pending.resolve(data);
                            }
                        } catch (e) {
                            pending.reject(e);
                        }
                    });
                    pendingQueue = [];

                } catch (refreshErr) {
                    // On refresh failure, logout
                    if (configuredLogout && configuredNavigate) {
                        configuredLogout();
                        configuredNavigate('/login', { replace: true });
                    }
                    // Reject all queued requests
                    pendingQueue.forEach((p) => p.reject(refreshErr));
                    pendingQueue = [];
                    throw refreshErr;
                } finally {
                    isRefreshing = false;
                }
            } else {
                if (configuredLogout && configuredNavigate) {
                    configuredLogout();
                    configuredNavigate('/login', { replace: true });
                }
                throw new Error('Unauthorized or expired token.');
            }
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `API call failed with status ${response.status}`);
        }

        const text = await response.text();
        return text ? JSON.parse(text) : null;

    } catch (error) {
        if (error.message !== 'Unauthorized or expired token.') {
            console.error('Error in fetchWrapper:', error);
        }
        throw error;
    }
};