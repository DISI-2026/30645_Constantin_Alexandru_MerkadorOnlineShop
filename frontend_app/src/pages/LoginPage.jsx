// src/pages/LoginPage.jsx

import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/LoginPage.css'; // Import CSS file

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
        const activeRole = await login(email, password);
        if (activeRole === 'ADMIN') navigate('/admin');
        else if (activeRole === 'SELLER') navigate('/seller');
        else navigate('/buyer');
    } catch (err) {
        // Check for backend's specific ExceptionHandlerResponseDTO format
        if (err.resource === 'PENDING_VERIFICATION') {
            navigate(`/verify?email=${encodeURIComponent(email)}`);

        } else if (err.resource === 'SUSPENDED') {
            navigate('/home', { state: { showSuspendedAlert: true } });

        } else if (err.resource === 'DEACTIVATED') {
            navigate('/reactivate-account', { state: { email } });

        } else {
            // Fallback to error message
            setError(err.message || 'Invalid credentials. Please try again.');
        }
    } finally {
        setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      <form onSubmit={handleSubmit} className="login-form">
        <div className="form-group">
          <label htmlFor="email">Email:</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={isLoading}
          />
        </div>
        <div className="form-group">
          <label htmlFor="password">Password:</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={isLoading}
          />
        </div>
        <div style={{ textAlign: 'right', marginBottom: '0.5rem' }}>
          <Link to="/forgot-password">Forgot Password?</Link>
        </div>
        {error && <p className="error-message">{error}</p>}
        <button type="submit" disabled={isLoading} className="login-button">
          {isLoading ? 'Logging In...' : 'Login'}
        </button>
        <div className="signup-link-container">
          <p>
            Don't have an account? <Link to="/register" className="signup-link">Sign up here</Link>
          </p>
        </div>
      </form>
    </div>
  );
};

export default LoginPage;
