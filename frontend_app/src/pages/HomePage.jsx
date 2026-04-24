// src/pages/HomePage.jsx
import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import '../styles/HomePage.css';
import reactLogo from '../assets/react-yellow.svg';

const HomePage = () => {
    const { logout, role, userUsername } = useAuth();

    return (
        <div className="home-page">
            <div className="home-container">
                <div className="home-header">
                    <h1 className="home-title">⚡ Energy Home ⚡</h1>
                    <p className="home-text">
                        Welcome back, {" "}
                        <span className="home-username">{userUsername}</span>.
                    </p>

                    <img
                        src={reactLogo}
                        alt="React Logo"
                        className="react-logo"
                    />
                </div>

                <div className="home-footer">
                    {role === 'ADMIN' && (
                        <Link to="/admin" className="home-link admin-link">
                            Go to Admin Page
                        </Link>
                    )}
                    {role === 'CLIENT' && (
                        <Link to="/client" className="home-link admin-link">
                            Go to Client Page
                        </Link>
                    )}

                    <button onClick={logout} className="logout-button">
                        Log Out
                    </button>
                </div>
            </div>
        </div>
    );
};

export default HomePage;
