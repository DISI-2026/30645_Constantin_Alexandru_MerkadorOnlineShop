import React, { useEffect, useRef, useState } from 'react';
import { useNotifications } from '../context/NotificationContext';
import '../styles/NotificationBell.css';

const TYPE_ICON = {
    ORDER_PLACED: '🛍️',
    ORDER_STATUS_CHANGED: '📦',
    REVIEW_POSTED: '⭐',
    VERIFICATION_REQUEST: '🛡️',
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
        userNotifications,
        adminNotifications,
        totalUnreadCount,
        userUnreadCount,
        adminUnreadCount,
        isAdmin,
        isLoading,
        userHasMore,
        adminHasMore,
        markAsRead,
        markAllAsRead,
        deleteNotification,
        loadMoreUser,
        loadMoreAdmin,
    } = useNotifications();

    const [open, setOpen] = useState(false);
    const [activeTab, setActiveTab] = useState('PERSONAL');
    const panelRef = useRef(null);
    const buttonRef = useRef(null);

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

    const handleItemClick = (n) => {
        if (!n.read) {
            markAsRead(n.id, activeTab === 'ADMIN');
        }
    };

    const handleDelete = (e, id) => {
        e.stopPropagation();
        deleteNotification(id, activeTab === 'ADMIN');
    };

    const currentNotifications = activeTab === 'PERSONAL' ? userNotifications : adminNotifications;
    const currentHasMore      = activeTab === 'PERSONAL' ? userHasMore : adminHasMore;
    const currentLoadMore     = activeTab === 'PERSONAL' ? loadMoreUser : loadMoreAdmin;

    return (
        <div className="notif-bell">
            <button
                ref={buttonRef}
                className={`notif-bell-btn${totalUnreadCount > 0 ? ' has-unread' : ''}`}
                onClick={() => setOpen(o => !o)}
                aria-label={`Notifications${totalUnreadCount > 0 ? ` (${totalUnreadCount} unread)` : ''}`}
                title="Notifications"
            >
                <BellIcon />
                {totalUnreadCount > 0 && (
                    <span className="notif-badge">
                        {totalUnreadCount > 99 ? '99+' : totalUnreadCount}
                    </span>
                )}
            </button>

            {open && (
                <div ref={panelRef} className="notif-panel">

                    <div className="notif-panel-header">
                        <span className="notif-panel-title">Notifications</span>
                        <div className="notif-panel-header-actions">
                            {/* Only show mark-all on personal tab */}
                            {activeTab === 'PERSONAL' && userUnreadCount > 0 && (
                                <button className="notif-mark-all-btn" onClick={markAllAsRead}>
                                    Mark all read
                                </button>
                            )}
                        </div>
                    </div>

                    {isAdmin && (
                        <div className="notif-tabs">
                            <button
                                className={`notif-tab${activeTab === 'PERSONAL' ? ' active' : ''}`}
                                onClick={() => setActiveTab('PERSONAL')}
                            >
                                Personal
                                {userUnreadCount > 0 && (
                                    <span className="notif-tab-badge">{userUnreadCount}</span>
                                )}
                            </button>
                            <button
                                className={`notif-tab${activeTab === 'ADMIN' ? ' active admin' : ''}`}
                                onClick={() => setActiveTab('ADMIN')}
                            >
                                Admin inbox
                                {adminUnreadCount > 0 && (
                                    <span className="notif-tab-badge">{adminUnreadCount}</span>
                                )}
                            </button>
                        </div>
                    )}

                    <div className="notif-list">
                        {currentNotifications.length === 0 && !isLoading ? (
                            <div className="notif-empty">
                                <span className="notif-empty-icon">🔔</span>
                                <span className="notif-empty-text">You're all caught up!</span>
                            </div>
                        ) : (
                            currentNotifications.map(n => (
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
                                            <span className="notif-title">
                                                {n.title || n.type.replace(/_/g, ' ')}
                                            </span>
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

                    {(currentHasMore || isLoading) && (
                        <div className="notif-panel-footer">
                            <button
                                className="notif-load-more-btn"
                                onClick={currentLoadMore}
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
