import React, { useEffect, useRef, useState } from 'react';
import { useNotifications } from '../context/NotificationContext';
import '../styles/NotificationBell.css';

const TYPE_ICON = {
    ORDER_PLACED: '🛍️',
    ORDER_STATUS_CHANGED: '📦',
    REVIEW_POSTED: '⭐',
};

const timeAgo = (dateString) => {
    if (!dateString) return '';
    const diff = Math.floor((Date.now() - new Date(dateString)) / 1000);
    if (diff < 60)     return 'Just now';
    if (diff < 3600)   return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400)  return `${Math.floor(diff / 3600)}h ago`;
    if (diff < 604800) return `${Math.floor(diff / 86400)}d ago`;
    return new Date(dateString).toLocaleDateString();
};

const BellIcon = () => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
    </svg>
);

const NotificationBell = () => {
    const {
        notifications, unreadCount, isConnected,
        isLoading, hasMore,
        markAsRead, markAllAsRead, deleteNotification, loadMore,
    } = useNotifications();

    const [open, setOpen] = useState(false);
    const panelRef = useRef(null);
    const buttonRef = useRef(null);

    // Close panel when clicking outside
    useEffect(() => {
        if (!open) return;
        const handleClick = (e) => {
            if (
                panelRef.current && !panelRef.current.contains(e.target) &&
                buttonRef.current && !buttonRef.current.contains(e.target)
            ) {
                setOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClick);
        return () => document.removeEventListener('mousedown', handleClick);
    }, [open]);

    const handleItemClick = (notification) => {
        if (!notification.read) {
            markAsRead(notification.id);
        }
    };

    const handleDelete = (e, id) => {
        e.stopPropagation();
        deleteNotification(id);
    };

    return (
        <div className="notif-bell">
            <button
                ref={buttonRef}
                className={`notif-bell-btn${unreadCount > 0 ? ' has-unread' : ''}`}
                onClick={() => setOpen(o => !o)}
                aria-label={`Notifications${unreadCount > 0 ? ` (${unreadCount} unread)` : ''}`}
                title="Notifications"
            >
                <BellIcon />
                {unreadCount > 0 && (
                    <span className="notif-badge">
                        {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                )}
            </button>

            {open && (
                <div ref={panelRef} className="notif-panel">
                    <div className="notif-panel-header">
                        <span className="notif-panel-title">Notifications</span>
                        <div className="notif-panel-header-actions">
                            {unreadCount > 0 && (
                                <button className="notif-mark-all-btn" onClick={markAllAsRead}>
                                    Mark all read
                                </button>
                            )}
                            <span
                                className={`notif-connected-dot${isConnected ? '' : ' disconnected'}`}
                                title={isConnected ? 'Live' : 'Reconnecting…'}
                            />
                        </div>
                    </div>

                    <div className="notif-list">
                        {notifications.length === 0 && !isLoading ? (
                            <div className="notif-empty">
                                <span className="notif-empty-icon">🔔</span>
                                <span className="notif-empty-text">You're all caught up!</span>
                            </div>
                        ) : (
                            notifications.map(n => (
                                <div
                                    key={n.id}
                                    className={`notif-item${n.read ? '' : ' unread'}`}
                                    onClick={() => handleItemClick(n)}
                                >
                                    <div className={`notif-type-icon ${n.type}`}>
                                        {TYPE_ICON[n.type] ?? '🔔'}
                                    </div>
                                    <div className="notif-content">
                                        <div className="notif-title-row">
                                            <span className="notif-title">{n.title}</span>
                                            <span className="notif-time">{timeAgo(n.createdAt)}</span>
                                        </div>
                                        <p className="notif-message">{n.message}</p>
                                    </div>
                                    {!n.read && <span className="notif-unread-dot" />}
                                    <button
                                        className="notif-delete-btn"
                                        onClick={(e) => handleDelete(e, n.id)}
                                        title="Delete"
                                    >
                                        ✕
                                    </button>
                                </div>
                            ))
                        )}
                    </div>

                    {(hasMore || isLoading) && (
                        <div className="notif-panel-footer">
                            <button
                                className="notif-load-more-btn"
                                onClick={loadMore}
                                disabled={isLoading}
                            >
                                {isLoading ? 'Loading…' : 'Load more'}
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default NotificationBell;
