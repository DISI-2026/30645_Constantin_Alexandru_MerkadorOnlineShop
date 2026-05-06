// src/context/AuthContext.jsx

import React, { createContext, useContext, useMemo, useState } from 'react';
import { login as apiLogin, logout as apiLogout, switchRole as apiSwitchRole } from '../api/credentialsService.js';
import { getUserById } from '../api/userService.js';

const AuthContext = createContext(null);

// Storage keys
const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_ID_KEY = 'userId';
const USER_EMAIL_KEY = 'userEmail';
const ACTIVE_ROLE_KEY = 'activeRole'; // 'CLIENT' | 'ADMIN'
const ROLES_KEY = 'roles'; // JSON stringified array
const FIRST_NAME_KEY = 'firstName';
const LAST_NAME_KEY = 'lastName';

export const useAuth = () => useContext(AuthContext);

// Basic JWT decode (no verification) to access claims such as sub
const decodeJwt = (token) => {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
    return decoded || {};
  } catch {
    return {};
  }
};

export const AuthProvider = ({ children }) => {
  const [accessToken, setAccessToken] = useState(localStorage.getItem(ACCESS_TOKEN_KEY));
  const [refreshToken, setRefreshToken] = useState(localStorage.getItem(REFRESH_TOKEN_KEY));
  const [userId, setUserId] = useState(localStorage.getItem(USER_ID_KEY));
  const [email, setEmail] = useState(localStorage.getItem(USER_EMAIL_KEY));
  const [activeRole, setActiveRoleState] = useState(localStorage.getItem(ACTIVE_ROLE_KEY));
  const [roles, setRoles] = useState(() => {
    const raw = localStorage.getItem(ROLES_KEY);
    try { return raw ? JSON.parse(raw) : []; } catch { return []; }
  });
  const [firstName, setFirstName] = useState(localStorage.getItem(FIRST_NAME_KEY));
  const [lastName, setLastName] = useState(localStorage.getItem(LAST_NAME_KEY));

  const isAuthenticated = !!accessToken;

  const setTokens = (newAccess, newRefresh) => {
    if (newAccess) {
      localStorage.setItem(ACCESS_TOKEN_KEY, newAccess);
      setAccessToken(newAccess);
    }
    if (newRefresh) {
      localStorage.setItem(REFRESH_TOKEN_KEY, newRefresh);
      setRefreshToken(newRefresh);
    }
  };

  const getRefreshToken = () => refreshToken;

  const login = async (emailInput, password) => {
    const data = await apiLogin(emailInput, password);
    // Expected: { token, refreshToken, id, email, activeRole, roles }
    const { token: at, refreshToken: rt, id, email: em, activeRole: ar, roles: rs } = data || {};
    if (!at || !rt) throw new Error('Invalid auth response');

    // Persist access token and get user profile metadata
    localStorage.setItem(ACCESS_TOKEN_KEY, at);

      let fn = null;
      let ln = null;
      try {
          // we get first name and last name from user-ms
          const userProfile = await getUserById(id);
          fn = userProfile.firstName;
          ln = userProfile.lastName;
      } catch (error) {
          console.error("Could not find user profile:", error);
      }

    // persist the rest of the data
    localStorage.setItem(REFRESH_TOKEN_KEY, rt);
    if (id) localStorage.setItem(USER_ID_KEY, id);
    if (em) localStorage.setItem(USER_EMAIL_KEY, em);
    if (ar) localStorage.setItem(ACTIVE_ROLE_KEY, ar);
    if (Array.isArray(rs)) localStorage.setItem(ROLES_KEY, JSON.stringify(rs));
    if (fn) localStorage.setItem(FIRST_NAME_KEY, fn);
    if (ln) localStorage.setItem(LAST_NAME_KEY, ln);

    setAccessToken(at);
    setRefreshToken(rt);
    setUserId(id);
    setEmail(em || null);
    setActiveRoleState(ar || null);
    setRoles(Array.isArray(rs) ? rs : []);
    setFirstName(fn || null);
    setLastName(ln || null);

    return ar || null; // return activeRole for redirect logic
  };

  const logout = async () => {
    try {
      if (refreshToken) {
        await apiLogout(refreshToken);
      }
    } catch (e) {
      // ignore network/api errors on logout; proceed to clear state
      console.warn('Logout API call failed or returned error:', e?.message);
    }

    // Clear storage
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(USER_EMAIL_KEY);
    localStorage.removeItem(ACTIVE_ROLE_KEY);
    localStorage.removeItem(ROLES_KEY);
    localStorage.removeItem(FIRST_NAME_KEY);
    localStorage.removeItem(LAST_NAME_KEY);

    // Clear state
    setAccessToken(null);
    setRefreshToken(null);
    setUserId(null);
    setEmail(null);
    setActiveRoleState(null);
    setRoles([]);
    setFirstName(null);
    setLastName(null);
  };

  const switchRole = async (targetRole) => {
    try {
        // request a new token with the new role
        const response = await apiSwitchRole(email, targetRole);

        // get new token
        const newJwt = response.token;
        if (!newJwt) {
            throw new Error("No token received from backend");
        }

        // save to localStorage
        localStorage.setItem(ACCESS_TOKEN_KEY, newJwt);
        localStorage.setItem(ACTIVE_ROLE_KEY, targetRole);

        // No need to explicitly update state here,
        // because AuthContext.Provider will re-render with the new token.

        // Hard redirect to the new role's page to avoid state delay
        if (targetRole === 'BUYER') {
            window.location.href = '/buyer';
        } else if (targetRole === 'SELLER') {
            window.location.href = '/seller';
        } else if (targetRole === 'ADMIN') {
            window.location.href = '/admin';
        }

    } catch (error) {
        console.error("Failed to switch role:", error);
        alert("Could not switch role. Please try again.");
    }
  };

  const decoded = useMemo(() => (accessToken ? decodeJwt(accessToken) : {}), [accessToken]);

  const value = {
    // tokens
    accessToken,
    refreshToken,
    setTokens,
    getRefreshToken,
    // user info
    userId,
    email: email || decoded.sub || null,
    activeRole,
    roles,
    firstName,
    lastName,
    // state & actions
    isAuthenticated,
    login,
    logout,
    switchRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};