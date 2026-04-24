import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import '../styles/ClientPage.css';

const ClientPage = () => {
    const { logout, userId } = useAuth();

    return (
        <div className="client-page-container">
            <div className="header-section">
                <h1 className="header-title">👤 Client Dashboard</h1>
            </div>

            <div className="content-section">
                {/* Un mesaj simplu de bun venit în locul graficului */}
                <div className="welcome-message" style={{ textAlign: 'center', padding: '2rem' }}>
                    <h2>Welcome to your dashboard!</h2>
                    <p>Select an action from the menu to get started.</p>
                </div>
            </div>

            <div className="footer-actions">
                <Link to="/" className="action-link">Go to Home</Link>
                <button onClick={logout} className="action-button logout-button">Logout</button>
            </div>
        </div>
    );
};

export default ClientPage;