import { fetchWrapper } from '../utils/fetchWrapper';

const BASE = '/api/notifications';

export const getNotifications = (page = 0, size = 20) =>
    fetchWrapper(`${BASE}?page=${page}&size=${size}`);

export const getUnreadCount = () =>
    fetchWrapper(`${BASE}/unread-count`);

export const markAsRead = (id) =>
    fetchWrapper(`${BASE}/${id}/read`, { method: 'PUT' });

export const markAllAsRead = () =>
    fetchWrapper(`${BASE}/read-all`, { method: 'PUT' });

export const deleteNotification = (id) =>
    fetchWrapper(`${BASE}/${id}`, { method: 'DELETE' });
