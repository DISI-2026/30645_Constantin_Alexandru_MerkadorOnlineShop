import React, { useState } from 'react';
import MainNavbar from '../components/MainNavbar.jsx';

const SellerPage = () => {
    const [tab, setTab] = useState('shop');

    const tabStyle = (isActive) => ({
        padding: '1rem 2rem',
        border: 'none',
        borderBottom: isActive ? '3px solid #e67e22' : '3px solid transparent', // Portocaliu pentru seller
        background: 'transparent',
        cursor: 'pointer',
        fontWeight: isActive ? 700 : 500,
        fontSize: '1.1rem',
        color: isActive ? '#d35400' : '#6c757d',
        transition: 'all 0.2s ease',
    });

    return (
        <div style={{ minHeight: '100vh', width: '100vw', backgroundColor: '#f4f6f8', margin: 0, padding: 0, color: '#333', textAlign: 'left' }}>
            <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '2rem 5%' }}>

                <MainNavbar pageTitle="Seller Dashboard" />

                {/* Main Content Card */}
                <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', boxShadow: '0 8px 24px rgba(0,0,0,0.05)', padding: '2rem', marginTop: '1rem', minHeight: '60vh' }}>

                    {/* Tabs */}
                    <div style={{ borderBottom: '1px solid #e1e1e1', marginBottom: '2rem', display: 'flex', gap: '1rem' }}>
                        <button style={tabStyle(tab === 'shop')} onClick={() => setTab('shop')}>Shop Profile</button>
                        <button style={tabStyle(tab === 'inventory')} onClick={() => setTab('inventory')}>My Products</button>
                        <button style={tabStyle(tab === 'sales')} onClick={() => setTab('sales')}>Sales & Orders</button>
                    </div>

                    {/* Content */}
                    {tab === 'shop' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Shop Configuration</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem' }}>Here the seller updates shopName, shopSlug, description, and sees verification status.</p>
                        </div>
                    )}
                    {tab === 'inventory' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Inventory Management</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem' }}>Add, edit, or remove products to sell.</p>
                        </div>
                    )}
                    {tab === 'sales' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Incoming Orders</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem' }}>Manage orders placed by buyers for your products.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SellerPage;