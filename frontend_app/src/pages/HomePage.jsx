// src/pages/HomePage.jsx
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import '../styles/HomePage.css';

// ==========================================
// 4. MAIN HOMEPAGE COMPONENT
// ==========================================
const HomePage = () => {
    const { logout, activeRole, firstName, lastName, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const handleAuthSelect = (e) => {
        const route = e.target.value;
        if (route) {
            navigate(route);
        }
    };

    return (
        <div className="home-layout">
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