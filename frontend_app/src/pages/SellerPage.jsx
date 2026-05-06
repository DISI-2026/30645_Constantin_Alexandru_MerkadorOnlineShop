import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import MainNavbar from '../components/MainNavbar.jsx';
import '../styles/SellerPage.css';

const SellerPage = () => {
  const [tab, setTab] = useState('shop');

  const tabStyle = (isActive) => ({
    padding: '1rem 2rem',
    border: 'none',
    borderBottom: isActive ? '3px solid #e67e22' : '3px solid transparent',
    background: 'transparent',
    cursor: 'pointer',
    fontWeight: isActive ? 700 : 500,
    fontSize: '1.1rem',
    color: isActive ? '#d35400' : '#6c757d',
    transition: 'all 0.2s ease',
  });

  return (
    <div className="seller-page">
      <div className="seller-page-container">
        <MainNavbar pageTitle="Seller Dashboard" />

        <section className="seller-page-hero">
          <div>
            <span className="seller-page-badge">Seller mode</span>
            <h1>Manage your shop</h1>
            <p>
              From here, you can manage your business profile, listed products,
              stock, prices, images, and incoming customer orders.
            </p>
          </div>

          <Link to="/seller/products" className="seller-page-primary-btn">
            Manage products
          </Link>
        </section>

        <div className="seller-main-card">
          <div className="seller-tabs">
            <button style={tabStyle(tab === 'shop')} onClick={() => setTab('shop')}>
              Shop Profile
            </button>

            <button style={tabStyle(tab === 'inventory')} onClick={() => setTab('inventory')}>
              My Products
            </button>

            <button style={tabStyle(tab === 'sales')} onClick={() => setTab('sales')}>
              Sales & Orders
            </button>
          </div>

          {tab === 'shop' && (
            <div className="seller-tab-content">
              <h2>Shop Configuration</h2>
              <p>
                Here, sellers will be able to update their shop name, description,
                logo, and verification status.
              </p>
            </div>
          )}

          {tab === 'inventory' && (
            <div className="seller-tab-content">
              <h2>Inventory Management</h2>
              <p>
                Manage your products, upload images, update stock levels, change
                prices, and activate or deactivate listings.
              </p>

              <Link to="/seller/products" className="seller-inventory-btn">
                Go to my products
              </Link>
            </div>
          )}

          {tab === 'sales' && (
            <div className="seller-tab-content">
              <h2>Incoming Orders</h2>
              <p>
                Here, sellers will be able to view and manage orders received
                from buyers.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SellerPage;