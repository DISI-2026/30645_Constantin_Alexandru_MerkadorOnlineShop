import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import MainNavbar from '../components/MainNavbar.jsx';
import AdminUserDashboard from '../components/AdminUserDashboard.jsx';
import StatisticsView from '../components/StatisticsView.jsx';

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

                {/* Manage categories section*/}
                <section style={{
                    background: 'linear-gradient(135deg, #111827 0%, #1f2937 100%)',
                    color: 'white',
                    borderRadius: '16px',
                    padding: '32px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    gap: '28px',
                    boxShadow: '0 18px 45px rgba(15, 23, 42, 0.16)',
                    marginBottom: '24px',
                    marginTop: '1.5rem',
                    flexWrap: 'wrap'
                }}>
                    <div>
                        <span style={{
                            display: 'inline-flex',
                            background: 'rgba(13, 110, 253, 0.2)',
                            color: '#90c2ff',
                            padding: '6px 12px',
                            borderRadius: '999px',
                            fontWeight: 800,
                            marginBottom: '14px',
                            fontSize: '0.85rem'
                        }}>
                            Catalogue Management
                        </span>
                        <h2 style={{ margin: 0, fontSize: '28px', fontWeight: 800, color: 'white' }}>Manage Categories</h2>
                        <p style={{ margin: '12px 0 0', color: '#cbd5e1', maxWidth: '600px', fontSize: '1.05rem' }}>
                            Define and organize the global product categories available for sellers to use across the entire platform.
                        </p>
                    </div>

                    <Link
                        to="/admin/categories"
                        style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            backgroundColor: '#0d6efd',
                            color: 'white',
                            padding: '14px 24px',
                            borderRadius: '12px',
                            textDecoration: 'none',
                            fontWeight: 700,
                            whiteSpace: 'nowrap',
                            transition: 'background-color 0.2s ease'
                        }}
                        onMouseOver={(e) => e.target.style.backgroundColor = '#0b5ed7'}
                        onMouseOut={(e) => e.target.style.backgroundColor = '#0d6efd'}
                    >
                        Manage Categories
                    </Link>
                </section>

                <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', boxShadow: '0 8px 24px rgba(0,0,0,0.05)', padding: '2rem', minHeight: '50vh' }}>

                    <div style={{ borderBottom: '1px solid #e1e1e1', marginBottom: '2rem', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                        <button style={tabStyle(activeTab === 'users')} onClick={() => setActiveTab('users')}>
                            User Dashboard
                        </button>

                        <button style={tabStyle(activeTab === 'statistics')} onClick={() => setActiveTab('statistics')}>
                            Statistics
                        </button>
                    </div>

                    {activeTab === 'users' && (
                        <div className="fade-in">
                            <AdminUserDashboard />
                        </div>
                    )}

                    {activeTab === 'statistics' && (
                        <div className="fade-in">
                            <StatisticsView />
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminPage;