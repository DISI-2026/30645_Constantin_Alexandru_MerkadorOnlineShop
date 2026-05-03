import React, { useState } from 'react';
import MainNavbar from '../components/MainNavbar.jsx';

const AdminPage = () => {
    const [activeTab, setActiveTab] = useState('users');

    const tabStyle = (isActive) => ({
        padding: '1rem 2rem',
        border: 'none',
        borderBottom: isActive ? '3px solid #0d6efd' : '3px solid transparent',
        background: 'transparent',
        cursor: 'pointer',
        fontWeight: isActive ? 700 : 500,
        fontSize: '1.1rem',
        color: isActive ? '#0d6efd' : '#6c757d',
        transition: 'all 0.2s ease',
    });

    return (
        <div style={{ minHeight: '100vh', width: '100vw', backgroundColor: '#f4f6f8', margin: 0, padding: 0, color: '#333', textAlign: 'left' }}>
            <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '2rem 5%' }}>

                <MainNavbar pageTitle="Admin Control Panel" />

                {/* Main Content Card */}
                <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', boxShadow: '0 8px 24px rgba(0,0,0,0.05)', padding: '2rem', marginTop: '1rem', minHeight: '60vh' }}>

                    {/* Tabs */}
                    <div style={{ borderBottom: '1px solid #e1e1e1', marginBottom: '2rem', display: 'flex', gap: '1rem' }}>
                        <button style={tabStyle(activeTab === 'users')} onClick={() => setActiveTab('users')}>
                            User Dashboard
                        </button>
                        <button style={tabStyle(activeTab === 'products')} onClick={() => setActiveTab('products')}>
                            Product Dashboard
                        </button>
                    </div>

                    {/* Content */}
                    {activeTab === 'users' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>User Dashboard</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem' }}>Here the admin will see and manage users (Approve seller profiles, ban users etc).</p>
                        </div>
                    )}
                    {activeTab === 'products' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Product Dashboard</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem' }}>Here the admin will manage product categories and global catalog.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminPage;