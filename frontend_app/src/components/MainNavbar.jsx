import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import NotificationBell from './NotificationBell';

const MainNavbar = ({ pageTitle }) => {
    const { logout, activeRole, roles, switchRole } = useAuth();

    const handleRoleChange = (e) => {
        const selectedRole = e.target.value;
        if (selectedRole !== activeRole) {
            switchRole(selectedRole);
        }
    };

    return (
        <header style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            paddingBottom: '1rem',
            borderBottom: '2px solid #ddd',
            marginBottom: '2rem'
        }}>
            <h1 style={{ fontSize: '2.5rem', color: '#34495e', margin: 0 }}>
                {pageTitle}
            </h1>

            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>

                {/* Dropdown for role switching (Shows up if roles.length > 1) */}
                {roles && roles.length > 1 && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <label htmlFor="roleSwitch" style={{ fontWeight: '600', color: '#555', margin: 0 }}>
                            View as:
                        </label>
                        <select
                            id="roleSwitch"
                            value={activeRole || ''}
                            onChange={handleRoleChange}
                            style={{
                                padding: '0.5rem',
                                borderRadius: '6px',
                                border: '1px solid #ccc',
                                backgroundColor: '#fff',
                                color: '#333',
                                fontWeight: '500',
                                cursor: 'pointer',
                                fontSize: '0.95rem'
                            }}
                        >
                            {roles.map((role) => (
                                <option key={role} value={role}>
                                    {role}
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                <NotificationBell />

                {/* Cart Icon for BUYER */}
                {activeRole === 'BUYER' && (
                    <Link to="/cart" style={{ textDecoration: 'none' }}>
                        <button className="action-button" style={{
                            backgroundColor: '#2196F3',
                            color: 'white',
                            border: 'none',
                            padding: '10px 15px',
                            cursor: 'pointer',
                            borderRadius: '6px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }} title="Shopping Cart">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <circle cx="9" cy="21" r="1"></circle>
                                <circle cx="20" cy="21" r="1"></circle>
                                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                            </svg>
                        </button>
                    </Link>
                )}

                <Link to="/browse" style={{textDecoration: 'none'}}>
                    <button className="action-button" style={{
                        backgroundColor: '#34db90',
                        color: 'white',
                        border: 'none',
                        padding: '10px 20px',
                        cursor: 'pointer',
                        borderRadius: '6px'
                    }}>
                        Browse
                    </button>
                </Link>

                <Link to="/" style={{textDecoration: 'none'}}>
                    <button className="action-button" style={{
                        backgroundColor: '#3498db',
                        color: 'white',
                        border: 'none',
                        padding: '10px 20px',
                        cursor: 'pointer',
                        borderRadius: '6px'
                    }}>
                        Home
                    </button>
                </Link>
                <button onClick={logout} className="action-button logout-button" style={{ backgroundColor: '#e74c3c', color: 'white', border: 'none', borderRadius: '6px'}}>
                    Logout
                </button>
            </div>
        </header>
    );
};

export default MainNavbar;
