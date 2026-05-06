// src/pages/ReactivatePage.jsx
import React, { useState } from 'react';
import { useLocation, useNavigate, Navigate, Link } from 'react-router-dom';
import { reactivate } from "../api/credentialsService.js";
import '../styles/ReactivatePage.css';

const ReactivatePage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const email = location.state?.email;

    // Add a loading state to prevent double-clicks
    const [isLoading, setIsLoading] = useState(false);

    if (!email) {
        return <Navigate to="/login" replace />;
    }

    const handleReactivationRequest = async () => {
        setIsLoading(true);
        try {
            await reactivate(email);
            navigate(`/verify?email=${encodeURIComponent(email)}`);
        } catch (error) {
            console.error('Error sending activation email:', error);
            alert('Failed to reactivate account. Please try again later.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="reactivate-wrapper">
            <div className="reactivate-card">

                {/* Visual Icon (Warning/Lock) */}
                <div className="reactivate-icon-container">
                    <svg fill="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 2C9.243 2 7 4.243 7 7v3H6c-1.103 0-2 .897-2 2v8c0 1.103.897 2 2 2h12c1.103 0 2-.897 2-2v-8c0-1.103-.897-2-2-2h-1V7c0-2.757-2.243-5-5-5zM9 7c0-1.654 1.346-3 3-3s3 1.346 3 3v3H9V7zm7 13H8v-6h8v6z"/>
                    </svg>
                </div>

                <h2 className="reactivate-title">Account Deactivated</h2>

                <p className="reactivate-text">
                    The account associated with <strong>{email}</strong> is currently deactivated.
                    You need to verify your email address to restore access.
                </p>

                <button
                    onClick={handleReactivationRequest}
                    className="reactivate-button"
                    disabled={isLoading}
                >
                    {isLoading ? (
                        <>
                            <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                            Sending...
                        </>
                    ) : (
                        "Send Activation Email"
                    )}
                </button>

                <Link to="/login" className="reactivate-back-link">
                    Cancel and return to Login
                </Link>

            </div>
        </div>
    );
};

export default ReactivatePage;