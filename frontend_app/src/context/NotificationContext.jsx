import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from './AuthContext';
import * as notificationService from '../api/notificationService';

const NotificationContext = createContext(null);
const PAGE_SIZE = 20;

export const NotificationProvider = ({ children }) => {
    const { isAuthenticated, accessToken, roles = [] } = useAuth();
    const isAdmin = roles.includes('ADMIN');

    // --- Separate states for user and admin notifications---
    const [userNotifications, setUserNotifications] = useState([]);
    const [adminNotifications, setAdminNotifications] = useState([]);

    const [userUnreadCount, setUserUnreadCount] = useState(0);
    const [adminUnreadCount, setAdminUnreadCount] = useState(0);

    const [userPage, setUserPage] = useState(0);
    const [adminPage, setAdminPage] = useState(0);

    const [userHasMore, setUserHasMore] = useState(false);
    const [adminHasMore, setAdminHasMore] = useState(false);

    const [isConnected, setIsConnected] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const stompClientRef = useRef(null);

    const fetchInitial = useCallback(async () => {
        if (!isAuthenticated) return;
        setIsLoading(true);
        try {
            // Fetch USER
            const [uPage, uCount] = await Promise.all([
                notificationService.getNotifications(0, PAGE_SIZE),
                notificationService.getUnreadCount(),
            ]);
            setUserNotifications(uPage.content ?? []);
            setUserHasMore(!uPage.last);
            setUserPage(0);
            setUserUnreadCount(uCount.count ?? 0);

            // Fetch ADMIN
            if (isAdmin) {
                const [aPage, aCount] = await Promise.all([
                    notificationService.getAdminNotifications(0, PAGE_SIZE),
                    notificationService.getAdminUnreadCount(),
                ]);
                setAdminNotifications(aPage.content ?? []);
                setAdminHasMore(!aPage.last);
                setAdminPage(0);
                setAdminUnreadCount(aCount.count ?? 0);
            }
        } catch (err) {
            console.error('Failed to fetch initial notifications:', err);
        } finally {
            setIsLoading(false);
        }
    }, [isAuthenticated, isAdmin]);

    // Load More for USER
    const loadMoreUser = useCallback(async () => {
        if (isLoading || !userHasMore) return;
        setIsLoading(true);
        try {
            const nextPage = userPage + 1;
            const pageData = await notificationService.getNotifications(nextPage, PAGE_SIZE);
            setUserNotifications(prev => [...prev, ...(pageData.content ?? [])]);
            setUserHasMore(!pageData.last);
            setUserPage(nextPage);
        } catch (err) {
            console.error('Failed to load more user notifs:', err);
        } finally {
            setIsLoading(false);
        }
    }, [isLoading, userHasMore, userPage]);

    // Load More for ADMIN
    const loadMoreAdmin = useCallback(async () => {
        if (isLoading || !adminHasMore) return;
        setIsLoading(true);
        try {
            const nextPage = adminPage + 1;
            const pageData = await notificationService.getAdminNotifications(nextPage, PAGE_SIZE);
            setAdminNotifications(prev => [...prev, ...(pageData.content ?? [])]);
            setAdminHasMore(!pageData.last);
            setAdminPage(nextPage);
        } catch (err) {
            console.error('Failed to load more admin notifs:', err);
        } finally {
            setIsLoading(false);
        }
    }, [isLoading, adminHasMore, adminPage]);

    // WebSockets
    useEffect(() => {
        if (!isAuthenticated || !accessToken) {
            if (stompClientRef.current?.active) stompClientRef.current.deactivate();
            setIsConnected(false);
            return;
        }

        const client = new Client({
            webSocketFactory: () => new SockJS(`${window.location.origin}/api/notifications/ws`),
            connectHeaders: { Authorization: `Bearer ${accessToken}` },
            reconnectDelay: 5000,
            onConnect: () => {
                setIsConnected(true);

                // User channel
                client.subscribe('/user/queue/notifications', (message) => {
                    try {
                        const notif = JSON.parse(message.body);
                        setUserNotifications(prev => [notif, ...prev]);
                        setUserUnreadCount(prev => prev + 1);
                    } catch (e) { console.error(e); }
                });

                // Admin channel
                if (isAdmin) {
                    client.subscribe('/topic/admins', (message) => {
                        try {
                            const notif = JSON.parse(message.body);
                            setAdminNotifications(prev => [notif, ...prev]);
                            setAdminUnreadCount(prev => prev + 1);
                        } catch (e) { console.error(e); }
                    });
                }
            },
            onDisconnect: () => setIsConnected(false),
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            client.deactivate();
            stompClientRef.current = null;
            setIsConnected(false);
        };
    }, [isAuthenticated, accessToken, isAdmin]);

    useEffect(() => {
        if (isAuthenticated) {
            fetchInitial();
        } else {
            setUserNotifications([]);
            setAdminNotifications([]);
            setUserUnreadCount(0);
            setAdminUnreadCount(0);
        }
    }, [isAuthenticated, fetchInitial]);

    const markAsRead = useCallback(async (id, isFromAdminTab = false) => {
        try {
            if (isFromAdminTab) {
                await notificationService.markAdminAsRead(id);
                // ADMIN notifications are not stored in the user's notification list, so we need to update the unread count separately'
                setAdminNotifications(prev => {
                    const target = prev.find(n => n.id === id);
                    if (target && !target.read) setAdminUnreadCount(c => Math.max(0, c - 1));
                    return prev.map(n => n.id === id ? { ...n, read: true } : n);
                });
            } else {
                await notificationService.markAsRead(id);
                setUserNotifications(prev => {
                    const target = prev.find(n => n.id === id);
                    if (target && !target.read) setUserUnreadCount(c => Math.max(0, c - 1));
                    return prev.map(n => n.id === id ? { ...n, read: true } : n);
                });
            }
        } catch (err) { console.error('Failed to mark as read:', err); }
    }, []);

    // Affects only USER notifications
    const markAllAsRead = useCallback(async () => {
        try {
            await notificationService.markAllAsRead();
            setUserNotifications(prev => prev.map(n => ({ ...n, read: true })));
            setUserUnreadCount(0);
        } catch (err) { console.error(err); }
    }, []);

    const deleteNotification = useCallback(async (id, isFromAdminTab = false) => {
        try {
            if (isFromAdminTab) {
                await notificationService.deleteAdminNotification(id);
                // Admin notif state
                setAdminNotifications(prev => {
                    const target = prev.find(n => n.id === id);
                    if (target && !target.read) setAdminUnreadCount(c => Math.max(0, c - 1));
                    return prev.filter(n => n.id !== id);
                });
            } else {
                await notificationService.deleteNotification(id);
                setUserNotifications(prev => {
                    const target = prev.find(n => n.id === id);
                    if (target && !target.read) setUserUnreadCount(c => Math.max(0, c - 1));
                    return prev.filter(n => n.id !== id);
                });
            }
        } catch (err) {
            console.error('Failed to delete notification:', err);
        }
    }, []);

    return (
        <NotificationContext.Provider value={{
            userNotifications,
            adminNotifications,
            totalUnreadCount: userUnreadCount + adminUnreadCount,
            userUnreadCount,
            adminUnreadCount,
            isAdmin,
            isConnected,
            isLoading,
            userHasMore,
            adminHasMore,
            markAsRead,
            markAllAsRead,
            deleteNotification,
            loadMoreUser,
            loadMoreAdmin,
        }}>
            {children}
        </NotificationContext.Provider>
    );
};

export const useNotifications = () => useContext(NotificationContext);