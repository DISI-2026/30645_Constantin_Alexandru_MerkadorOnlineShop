// src/components/BrowseNavbar.jsx
import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import '../styles/BrowseNavbar.css';
import NotificationBell from './NotificationBell';

const BrowseNavbar = ({ pageTitle }) => {
    const { activeRole, logout } = useAuth();

    // Determine the correct dashboard route based on the user's active role
    const getDashboardRoute = () => {
        if (activeRole === 'ADMIN') return '/admin';
        if (activeRole === 'SELLER') return '/seller';
        return '/buyer'; // Default fallback
    };

    // Determine the correct button label
    const getDashboardLabel = () => {
        if (activeRole === 'ADMIN') return 'Admin Dashboard';
        if (activeRole === 'SELLER') return 'Seller Dashboard';
        return 'Buyer Dashboard'; // Default fallback
    };

    return (
        <header className="browse-header">
            <h1 className="browse-title">
                {pageTitle}
            </h1>

            <div className="browse-nav-buttons">
                <NotificationBell />

                {activeRole === 'BUYER' && (
                    <Link to="/cart" style={{ textDecoration: 'none' }}>
                        <button className="browse-action-button" style={{ backgroundColor: '#27ae60', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '10px 15px' }} title="Shopping Cart">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <circle cx="9" cy="21" r="1"></circle>
                                <circle cx="20" cy="21" r="1"></circle>
                                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                            </svg>
                        </button>
                    </Link>
                )}

                {/* Dynamic Dashboard Button */}
                <Link to={getDashboardRoute()} style={{ textDecoration: 'none' }}>
                    <button className="browse-action-button browse-dashboard-btn">
                        {getDashboardLabel()}
                    </button>
                </Link>

                {/* Home Button */}
                <Link to="/" style={{ textDecoration: 'none' }}>
                    <button className="browse-action-button browse-home-btn">
                        Home
                    </button>
                </Link>

                {/* Logout Button */}
                <button
                    onClick={logout}
                    className="browse-action-button browse-logout-btn"
                >
                    Logout
                </button>
            </div>
        </header>
    );
};

export default BrowseNavbar;
