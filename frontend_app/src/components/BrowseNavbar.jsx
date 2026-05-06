// src/components/BrowseNavbar.jsx
import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import '../styles/BrowseNavbar.css';

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