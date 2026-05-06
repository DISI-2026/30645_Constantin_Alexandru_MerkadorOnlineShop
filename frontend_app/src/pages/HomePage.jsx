// src/pages/HomePage.jsx
import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import '../styles/HomePage.css';

const HomePage = () => {
  const { logout, activeRole, firstName, lastName, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [showPopup, setShowPopup] = useState(false);

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

  const goToDashboard = () => {
    if (activeRole === 'ADMIN') navigate('/admin');
    else if (activeRole === 'BUYER') navigate('/buyer');
    else if (activeRole === 'SELLER') navigate('/seller');
  };

  return (
    <div className="home-layout">
      {showPopup && (
        <div className="home-alert">
          Your account is currently suspended. Please contact support.
        </div>
      )}

      <header className="topbar">
        <div className="topbar-left" onClick={() => navigate('/')}>
          <div className="brand-icon">M</div>
          <span className="brand-name">Merkador</span>
        </div>

        <div className="topbar-right">
          {isAuthenticated ? (
            <div className="user-controls">
              <span className="welcome-text">
                Welcome, <strong>{firstName} {lastName}</strong>
              </span>

              {activeRole === 'ADMIN' && (
                <Link to="/admin" className="nav-button">
                  Admin Dashboard
                </Link>
              )}

              {activeRole === 'BUYER' && (
                <Link to="/buyer" className="nav-button">
                  Buyer Dashboard
                </Link>
              )}

              {activeRole === 'SELLER' && (
                <Link to="/seller" className="nav-button">
                  Seller Dashboard
                </Link>
              )}

              <button
                type="button"
                onClick={() => navigate('/browse')}
                className="nav-button nav-button-primary"
              >
                Browse Products
              </button>

              <button
                type="button"
                onClick={logout}
                className="nav-button logout-button"
              >
                Log Out
              </button>
            </div>
          ) : (
            <div className="auth-controls">
              <button
                type="button"
                className="nav-button"
                onClick={() => navigate('/login')}
              >
                Login
              </button>

              <button
                type="button"
                className="nav-button nav-button-primary"
                onClick={() => navigate('/signup')}
              >
                Sign Up
              </button>

              <select className="auth-combobox" onChange={handleAuthSelect} defaultValue="">
                <option value="" disabled>More</option>
                <option value="/login">Login</option>
                <option value="/signup">Sign Up</option>
              </select>
            </div>
          )}
        </div>
      </header>

      <main className="home-main">
        <section className="hero-section">
          <div className="hero-content">
            <span className="hero-eyebrow">Online marketplace platform</span>

            <h1>
              Buy, sell and manage products easily with Merkador.
            </h1>

            <p>
              Discover available products, filter by category, price and rating,
              or manage your seller catalog from a clean dashboard.
            </p>

            <div className="hero-actions">
              <button
                type="button"
                className="hero-btn hero-btn-primary"
                onClick={() => navigate('/browse')}
              >
                Browse Products
              </button>

              {isAuthenticated ? (
                <button
                  type="button"
                  className="hero-btn hero-btn-secondary"
                  onClick={goToDashboard}
                >
                  Go to Dashboard
                </button>
              ) : (
                <button
                  type="button"
                  className="hero-btn hero-btn-secondary"
                  onClick={() => navigate('/signup')}
                >
                  Create Account
                </button>
              )}
            </div>

            {isAuthenticated && (
              <div className="hero-user-card">
                <span>Active role</span>
                <strong>{activeRole || 'No role selected'}</strong>
              </div>
            )}
          </div>

          <div className="hero-visual">
            <div className="market-card market-card-main">
              <div className="market-card-header">
                <span className="market-dot" />
                <span className="market-dot" />
                <span className="market-dot" />
              </div>

              <div className="market-product-preview">
                <div className="preview-image" />
                <div>
                  <h3>Featured Product</h3>
                  <p>Smartphone, laptop, accessories and more.</p>
                  <strong>From 99 RON</strong>
                </div>
              </div>

              <div className="market-stats">
                <div>
                  <strong>Categories</strong>
                  <span>Browse by interest</span>
                </div>

                <div>
                  <strong>Filters</strong>
                  <span>Price, rating, search</span>
                </div>
              </div>
            </div>

            <div className="floating-card floating-card-one">
              Fast checkout
            </div>

            <div className="floating-card floating-card-two">
              Seller tools
            </div>
          </div>
        </section>

        <section className="home-feature-grid">
          <article className="home-feature-card">
            <div className="feature-icon">🛒</div>
            <h3>Browse products</h3>
            <p>
              Search and filter products by category, price, rating and keywords.
            </p>
          </article>

          <article className="home-feature-card">
            <div className="feature-icon">📦</div>
            <h3>Sell your items</h3>
            <p>
              Sellers can create products, upload images and manage stock or price.
            </p>
          </article>

          <article className="home-feature-card">
            <div className="feature-icon">⚙️</div>
            <h3>Admin control</h3>
            <p>
              Admin users can manage categories and platform configuration.
            </p>
          </article>
        </section>
      </main>
    </div>
  );
};

export default HomePage;