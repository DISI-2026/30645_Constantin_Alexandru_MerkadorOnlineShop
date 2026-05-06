// src/pages/BrowsePage.jsx
import React from 'react';
import BrowseNavbar from '../components/BrowseNavbar.jsx';
import '../styles/BrowsePage.css';

const BrowsePage = () => {
    return (
        <div className="browse-page-wrapper">
            <div className="browse-page-container">

                {/* Specific Navbar for Browse Page */}
                <BrowseNavbar pageTitle="Browse Products" />

                {/* Main Card */}
                <div className="browse-content-card">
                    <h2 className="browse-section-title">Product Catalog</h2>
                    <p className="browse-section-subtitle">
                        Here you will be able to search, filter, and view all available products.
                    </p>

                    {/* Products grid goes here */}
                </div>

            </div>
        </div>
    );
};

export default BrowsePage;