// src/pages/VerificationPage.jsx
import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { activate, resendCode } from '../api/credentialsService';

const VerificationPage = () => {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const initialEmail = useMemo(() => params.get('email') || '', [params]);

  const [email, setEmail] = useState(initialEmail);
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const [cooldown, setCooldown] = useState(0); // seconds remaining

  useEffect(() => {
    let intervalId;
    if (cooldown > 0) {
      intervalId = setInterval(() => setCooldown((c) => c - 1), 1000);
    }
    return () => { if (intervalId) clearInterval(intervalId); };
  }, [cooldown]);

  const handleVerify = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    if (!email || !code) {
      setError('Please provide both email and verification code.');
      return;
    }
    setLoading(true);
    try {
      await activate(email.trim(), code.trim());
      setMessage('Account verified! You can now log in.');
      setTimeout(() => navigate('/login'), 1200);
    } catch (err) {
      setError(err.message || 'Verification failed. Please check the code and try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setError('');
    setMessage('');
    if (!email) {
      setError('Please enter your email first.');
      return;
    }
    setLoading(true);
    try {
      await resendCode(email.trim());
      setMessage('A new code has been sent.');
      setCooldown(60); // start 60s cooldown
    } catch (err) {
      setError(err.message || 'Failed to resend code.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container" style={{ maxWidth: 420 }}>
      <h2>Verify Your Account</h2>
      <form onSubmit={handleVerify} className="login-form">
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        <div className="form-group">
          <label htmlFor="code">Verification Code (6 digits)</label>
          <input
              id="code"
              type="text"
              pattern="[0-9]{6}" // FIX: Changed from "\\d{6}" to "[0-9]{6}"
              inputMode="numeric"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              required
              disabled={loading}
              placeholder="123456"
          />
        </div>

        {message && <p className="success-message" style={{ color: '#2ecc71' }}>{message}</p>}
        {error && <p className="error-message">{error}</p>}

        <button type="submit" disabled={loading} className="login-button">
          {loading ? 'Verifying...' : 'Verify'}
        </button>
      </form>

      <div style={{ marginTop: '1rem', textAlign: 'center' }}>
        <button
          type="button"
          onClick={handleResend}
          disabled={loading || cooldown > 0}
          className="login-button"
          style={{ opacity: (loading || cooldown > 0) ? 0.6 : 1 }}
        >
          {cooldown > 0 ? `Resend Code (${cooldown}s)` : 'Resend Code'}
        </button>
      </div>
    </div>
  );
};

export default VerificationPage;
