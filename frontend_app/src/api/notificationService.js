import { fetchWrapper } from '../utils/fetchWrapper';

const BASE = '/api/notifications';

export const getNotifications = (page = 0, size = 20) =>
    fetchWrapper(`${BASE}?page=${page}&size=${size}`);

export const getUnreadCount = () =>
    fetchWrapper(`${BASE}/unread-count`);

export const getAdminNotifications = (page = 0, size = 20) =>
    fetchWrapper(`${BASE}/admin?page=${page}&size=${size}`);

export const getAdminUnreadCount = () =>
    fetchWrapper(`${BASE}/admin/unread-count`);

export const sendVerificationRequest = (shopName, categories, message) =>
    fetchWrapper(`${BASE}/admin/verification-request`, {
        method: 'POST',
        body: JSON.stringify({ shopName, categories, message })
    });

export const markAsRead = (id) =>
    fetchWrapper(`${BASE}/${id}/read`, { method: 'PUT' });

export const markAllAsRead = () =>
    fetchWrapper(`${BASE}/read-all`, { method: 'PUT' });

export const markAdminAsRead = (id) =>
    fetchWrapper(`${BASE}/admin/${id}/read`, { method: 'PUT' });

export const deleteNotification = (id) =>
    fetchWrapper(`${BASE}/${id}`, { method: 'DELETE' });

export const deleteAdminNotification = (id) =>
    fetchWrapper(`${BASE}/admin/${id}`, { method: 'DELETE' });
