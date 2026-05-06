// src/pages/VerificationPage.jsx
import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { activate, resendCode, confirmEmailUpdate, requestEmailUpdate } from '../api/credentialsService';
import { useAuth } from '../context/AuthContext';

const VerificationPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { userId: loggedInUserId, logout} = useAuth(); // for userId fallback and to logout the user after a successful verification

    // Get query parameters
    const passedEmail = searchParams.get('email') || '';
    const passedType = searchParams.get('type') || '';
    const passedUserId = searchParams.get('userId') || loggedInUserId;

    // check if the user is trying to update their email or not
    const isEmailUpdate = passedType === 'emailUpdate';

    // Setup state
    const [email, setEmail] = useState(passedEmail);
    const [code, setCode] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');
    const [cooldown, setCooldown] = useState(0);

  // auto-complete email if it's an email update flow
  useEffect(() => {
    if (passedEmail) {
        setEmail(passedEmail);
    }
  }, [passedEmail]);

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
          if (isEmailUpdate) {
              // --- EMAIL UPDATE FLOW ---
              await confirmEmailUpdate(passedUserId, code.trim());
              setMessage('Email updated successfully! Redirecting to login page..');
          } else {
              // --- NORMAL ACTIVATION FLOW ---
              await activate(email.trim(), code.trim());
              setMessage('Account verified! Redirecting to login page..');
          }
          // Redirect to login for both cases, but logout first for email update flow
          setTimeout(async () => {
              if (isEmailUpdate) {
                  await logout();
              }
              navigate('/login');
          }, 2000);
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
          if (isEmailUpdate) {
              // --- EMAIL UPDATE FLOW ---
              await requestEmailUpdate(passedUserId, email.trim());
          } else {
              // --- NORMAL ACTIVATION FLOW ---
              await resendCode(email.trim());
          }

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
            <h2>{isEmailUpdate ? 'Verify New Email' : 'Verify Your Account'}</h2>
            <form onSubmit={handleVerify} className="login-form">
                <div className="form-group">
                    <label htmlFor="email">Email</label>
                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        disabled={loading || isEmailUpdate} // Prevent changing email input if it's an email update flow
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="code">Verification Code (6 digits)</label>
                    <input
                        id="code"
                        type="text"
                        pattern="[0-9]{6}"
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
