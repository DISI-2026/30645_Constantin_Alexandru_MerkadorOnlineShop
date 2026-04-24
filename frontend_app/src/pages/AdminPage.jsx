import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';

// Import the specialized component
import UserListTab from '../components/UserListTab';

const AdminPage = () => {
    const { logout, role, userUsername } = useAuth();

    return (
        <div className="admin-page-container">
            <header className="admin-header">
                <h1 className="header-title">🛡️ Admin Control Panel</h1>
                <div className="action-buttons">
                    <Link to="/" className="action-link home-link">Go to Home</Link>
                    <button onClick={logout} className="action-button logout-button">Logout</button>
                </div>
            </header>

            <div className="admin-content">
                <UserListTab
                    // You'll need a mechanism to refetch the user list after an edit,
                    // usually by passing a fetch function or a refresh key.
                />
            </div>

            {/* Note: CSS styling will be required for the admin-page-container, etc. */}
        </div>
    );
};

export default AdminPage;