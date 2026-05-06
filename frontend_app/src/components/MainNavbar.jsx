import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';

const MainNavbar = ({ pageTitle }) => {
    const { logout, activeRole, roles, switchRole } = useAuth();
    const navigate = useNavigate();

    const handleRoleChange = (e) => {
        const selectedRole = e.target.value;
        if (selectedRole !== activeRole) {
            switchRole(selectedRole, navigate);
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