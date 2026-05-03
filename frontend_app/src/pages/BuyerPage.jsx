import React, { useState } from 'react';
import MainNavbar from '../components/MainNavbar.jsx';

const BuyerPage = () => {
    const [tab, setTab] = useState('account');

    const tabStyle = (isActive) => ({
        padding: '1rem 2rem',
        border: 'none',
        borderBottom: isActive ? '3px solid #2ecc71' : '3px solid transparent', // Verde pentru buyer
        background: 'transparent',
        cursor: 'pointer',
        fontWeight: isActive ? 700 : 500,
        fontSize: '1.1rem',
        color: isActive ? '#27ae60' : '#6c757d',
        transition: 'all 0.2s ease',
    });

    return (
        <div style={{ minHeight: '100vh', width: '100vw', backgroundColor: '#f4f6f8', margin: 0, padding: 0, color: '#333', textAlign: 'left' }}>
            <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '2rem 5%' }}>

                <MainNavbar pageTitle="Buyer Dashboard" />

                {/* Main Content Card */}
                <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', boxShadow: '0 8px 24px rgba(0,0,0,0.05)', padding: '2rem', marginTop: '1rem', minHeight: '60vh' }}>

                    {/* Tabs */}
                    <div style={{ borderBottom: '1px solid #e1e1e1', marginBottom: '2rem', display: 'flex', gap: '1rem' }}>
                        <button style={tabStyle(tab === 'account')} onClick={() => setTab('account')}>Account</button>
                        <button style={tabStyle(tab === 'orders')} onClick={() => setTab('orders')}>My Orders</button>
                        <button style={tabStyle(tab === 'wishlist')} onClick={() => setTab('wishlist')}>Wishlist</button>
                    </div>

                    {/* Content */}
                    {tab === 'account' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Account Details</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem' }}>Here the user manages personal info, avatar, and Address Book.</p>
                        </div>
                    )}
                    {tab === 'orders' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Order History</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem' }}>List of past purchases.</p>
                        </div>
                    )}
                    {tab === 'wishlist' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Saved Items</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem' }}>Products the buyer wants to purchase later.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default BuyerPage;