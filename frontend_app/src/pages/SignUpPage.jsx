// src/pages/LoginPage.jsx

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createUser } from '../api/userService';
import '../styles/SignUpPage.css';

const SignUpPage = () => {
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [successMsg, setSuccessMsg] = useState('');
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        fullName: '',
        username: '',
        password: '',
        email: '',
        address: '', // optional
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            // We implicitly only allow clients to signup for an account (CLIENT)
            const userData = { ...formData, role: 'CLIENT' };

            const response = await createUser(userData);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Registration failed. Username or email might be taken.');
            }
            // set success message
            setSuccessMsg('Account created successfully! Redirecting to login...');

            // Redirect to login page after 2 seconds
            setTimeout(() => {
                navigate('/login');
            }, 2000);

        } catch (err) {
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="signup-container">
            <h2>Create Account</h2>
            <form onSubmit={handleSubmit} className="signup-form">
                <div className="form-group">
                    <label htmlFor="fullName">Full Name *</label>
                    <input type="text" id="fullName" name="fullName" value={formData.fullName} onChange={handleChange} required disabled={isLoading} />
                </div>

                <div className="form-group">
                    <label htmlFor="username">Username *</label>
                    <input type="text" id="username" name="username" value={formData.username} onChange={handleChange} required disabled={isLoading} />
                </div>

                <div className="form-group">
                    <label htmlFor="email">Email *</label>
                    <input type="email" id="email" name="email" value={formData.email} onChange={handleChange} required disabled={isLoading} />
                </div>

                <div className="form-group">
                    <label htmlFor="password">Password *</label>
                    <input type="password" id="password" name="password" value={formData.password} onChange={handleChange} required disabled={isLoading} />
                </div>

                <div className="form-group">
                    <label htmlFor="address">Address</label>
                    <input type="text" id="address" name="address" value={formData.address} onChange={handleChange} disabled={isLoading} />
                </div>

                {error && <p className="error-message">{error}</p>}
                {successMsg && <p className="success-message" style={{color: '#2ecc71', textAlign: 'center'}}>{successMsg}</p>}

                <button type="submit" disabled={isLoading} className="signup-button">
                    {isLoading ? 'Creating Account...' : 'Sign Up'}
                </button>
            </form>

            <div className="login-link-container">
                <p>Already have an account? <a href="/login" className="login-link">Login in here</a></p>
            </div>
        </div>
    );
};

export default SignUpPage;