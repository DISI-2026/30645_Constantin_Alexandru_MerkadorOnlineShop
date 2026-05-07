import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from './AuthContext';
import * as notificationService from '../api/notificationService';

const NotificationContext = createContext(null);

const PAGE_SIZE = 20;

export const NotificationProvider = ({ children }) => {
    const { isAuthenticated, accessToken } = useAuth();

    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [isConnected, setIsConnected] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(0);
    const [hasMore, setHasMore] = useState(false);

    const stompClientRef = useRef(null);

    // Fetch first page of notifications and unread count
    const fetchInitial = useCallback(async () => {
        if (!isAuthenticated) return;
        setIsLoading(true);
        try {
            const [page, countData] = await Promise.all([
                notificationService.getNotifications(0, PAGE_SIZE),
                notificationService.getUnreadCount(),
            ]);
            setNotifications(page.content ?? []);
            setHasMore(!page.last);
            setCurrentPage(0);
            setUnreadCount(countData.count ?? 0);
        } catch (err) {
            console.error('Failed to fetch notifications:', err);
        } finally {
            setIsLoading(false);
        }
    }, [isAuthenticated]);

    // Load next page
    const loadMore = useCallback(async () => {
        if (isLoading || !hasMore) return;
        const next = currentPage + 1;
        setIsLoading(true);
        try {
            const page = await notificationService.getNotifications(next, PAGE_SIZE);
            setNotifications(prev => [...prev, ...(page.content ?? [])]);
            setHasMore(!page.last);
            setCurrentPage(next);
        } catch (err) {
            console.error('Failed to load more notifications:', err);
        } finally {
            setIsLoading(false);
        }
    }, [isLoading, hasMore, currentPage]);

    // WebSocket lifecycle
    useEffect(() => {
        if (!isAuthenticated || !accessToken) {
            if (stompClientRef.current?.active) {
                stompClientRef.current.deactivate();
                stompClientRef.current = null;
            }
            setIsConnected(false);
            return;
        }

        const token = accessToken;

        const client = new Client({
            webSocketFactory: () => new SockJS(`${window.location.origin}/api/notifications/ws`),
            connectHeaders: { Authorization: `Bearer ${token}` },
            reconnectDelay: 5000,
            onConnect: () => {
                setIsConnected(true);
                client.subscribe('/user/queue/notifications', (message) => {
                    try {
                        const notification = JSON.parse(message.body);
                        setNotifications(prev => [notification, ...prev]);
                        setUnreadCount(prev => prev + 1);
                    } catch (e) {
                        console.error('Failed to parse incoming notification:', e);
                    }
                });
            },
            onDisconnect: () => setIsConnected(false),
            onStompError: (frame) => console.error('STOMP error:', frame.headers?.message),
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            client.deactivate();
            stompClientRef.current = null;
            setIsConnected(false);
        };
    }, [isAuthenticated, accessToken]);

    // Fetch notifications when auth state changes
    useEffect(() => {
        if (isAuthenticated) {
            fetchInitial();
        } else {
            setNotifications([]);
            setUnreadCount(0);
            setCurrentPage(0);
            setHasMore(false);
        }
    }, [isAuthenticated, fetchInitial]);

    const markAsRead = useCallback(async (id) => {
        try {
            const updated = await notificationService.markAsRead(id);
            setNotifications(prev =>
                prev.map(n => n.id === id ? { ...n, read: true } : n)
            );
            setUnreadCount(prev => Math.max(0, prev - 1));
            return updated;
        } catch (err) {
            console.error('Failed to mark notification as read:', err);
        }
    }, []);

    const markAllAsRead = useCallback(async () => {
        try {
            await notificationService.markAllAsRead();
            setNotifications(prev => prev.map(n => ({ ...n, read: true })));
            setUnreadCount(0);
        } catch (err) {
            console.error('Failed to mark all as read:', err);
        }
    }, []);

    const deleteNotification = useCallback(async (id) => {
        const target = notifications.find(n => n.id === id);
        try {
            await notificationService.deleteNotification(id);
            setNotifications(prev => prev.filter(n => n.id !== id));
            if (target && !target.read) {
                setUnreadCount(prev => Math.max(0, prev - 1));
            }
        } catch (err) {
            console.error('Failed to delete notification:', err);
        }
    }, [notifications]);

    return (
        <NotificationContext.Provider value={{
            notifications,
            unreadCount,
            isConnected,
            isLoading,
            hasMore,
            markAsRead,
            markAllAsRead,
            deleteNotification,
            loadMore,
        }}>
            {children}
        </NotificationContext.Provider>
    );
};

export const useNotifications = () => {
    const ctx = useContext(NotificationContext);
    if (!ctx) throw new Error('useNotifications must be used within NotificationProvider');
    return ctx;
};
