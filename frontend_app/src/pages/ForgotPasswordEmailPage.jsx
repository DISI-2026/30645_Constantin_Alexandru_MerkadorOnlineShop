// src/pages/ForgotPasswordEmailPage.jsx
import React, { useState } from 'react';
import { forgotPassword } from '../api/credentialsService';

const ForgotPasswordEmailPage = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');
    setLoading(true);
    try {
      await forgotPassword(email.trim());
      setMessage('If an account exists for this email, a reset link has been sent.');
    } catch (err) {
      setError(err.message || 'Failed to send reset email.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container" style={{ maxWidth: 420 }}>
      <h2>Forgot Password</h2>
      <form onSubmit={handleSubmit} className="login-form">
        <div className="form-group">
          <label htmlFor="email">Email:</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        {message && <p className="success-message" style={{ color: '#2ecc71' }}>{message}</p>}
        {error && <p className="error-message">{error}</p>}
        <button type="submit" disabled={loading} className="login-button">
          {loading ? 'Sending...' : 'Send Reset Link'}
        </button>
      </form>
    </div>
  );
};

export default ForgotPasswordEmailPage;
