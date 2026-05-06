// src/pages/HomePage.jsx
import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import '../styles/HomePage.css';

// ==========================================
// 4. MAIN HOMEPAGE COMPONENT
// ==========================================
const HomePage = () => {
    const { logout, activeRole, firstName, lastName, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const location = useLocation();
    const [showPopup, setShowPopup] = useState(false);

    // Runs when coming from LoginPage or fetchWrapper after the system found out the user is suspended
    useEffect(() => {
        if (location.state?.showSuspendedAlert) {
            setShowPopup(true);

            const timer = setTimeout(() => {
                setShowPopup(false);
            }, 5000);

            return () => clearTimeout(timer);
        }
    }, [location.state]);

    const handleAuthSelect = (e) => {
        const route = e.target.value;
        if (route) {
            navigate(route);
        }
    };

    return (
        <div className="home-layout">

            {/*Shows up if the user is suspended*/}
            {showPopup && (
                <div style={{
                    position: 'fixed',
                    top: '20px',
                    left: '50%',
                    transform: 'translateX(-50%)',
                    backgroundColor: '#e74c3c',
                    color: 'white',
                    padding: '15px 30px',
                    borderRadius: '8px',
                    boxShadow: '0 4px 15px rgba(0,0,0,0.2)',
                    zIndex: 9999,
                    fontWeight: 'bold',
                    animation: 'fadeSlideUp 0.3s ease-out'
                }}>
                    Your account is currently suspended. Please contact support.
                </div>
            )}

            <header className="topbar">
                <div className="topbar-left">
                    <span className="brand-name">Merkador</span>
                </div>

                <div className="topbar-right">
                    {isAuthenticated ? (
                        <div className="user-controls">
                            <span className="welcome-text">Welcome, <strong>{firstName} {lastName}</strong></span>

                            {/* Use activeRole for the conditional rendering */}
                            {activeRole === 'ADMIN' && <Link to="/admin" className="nav-button">Admin Dashboard</Link>}
                            {activeRole === 'BUYER' && <Link to="/buyer" className="nav-button">Buyer Dashboard</Link>}
                            {activeRole === 'SELLER' && <Link to="/seller" className="nav-button">Seller Dashboard</Link>}
                            <button onClick={() => navigate('/browse')}  className="btn btn-primary"> Browse Products </button>
                            <button onClick={logout} className="nav-button logout-button">Log Out</button>
                        </div>
                    ) : (
                        <div className="auth-controls">
                            <select className="auth-combobox" onChange={handleAuthSelect} defaultValue="">
                                <option value="" disabled>Login / Sign Up</option>
                                <option value="/login">Login</option>
                                <option value="/signup">Sign Up</option>
                            </select>
                        </div>
                    )}
                </div>
            </header>
        </div>
    );
};

export default HomePage;