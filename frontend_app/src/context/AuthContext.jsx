// src/context/AuthContext.jsx

import React, { createContext, useContext, useState } from 'react';
import { login as apiLogin } from '../api/credentialsService.js';

const AuthContext = createContext(null);

// **Key storage strategy:** Using localStorage for persistence across tabs/sessions.
const TOKEN_KEY = 'jwtToken';
const ROLE_KEY = 'userRole';
const ID_KEY = 'userId';
const USERNAME_KEY = 'userUsername';
/**
 * Custom hook to use the authentication context.
 * @returns {{
 * token: string | null,
 * role: string | null,
 * userId: string | null,
 * userUsername: string | null,
 * isAuthenticated: boolean,
 * login: (username: string, password: string) => Promise<void>,
 * logout: () => void
 * }}
 */
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
    const [token, setToken] = useState(localStorage.getItem(TOKEN_KEY));
    const [role, setRole] = useState(localStorage.getItem(ROLE_KEY));
    const [userId, setUserId] = useState(localStorage.getItem(ID_KEY));
    const [userUsername, setUserUsername] = useState(localStorage.getItem(USERNAME_KEY));
    const isAuthenticated = !!token;

    const login = async (username, password) => {
        try {
            // 1. Call the API service
            const { token: newToken, role: newRole, id: newId, username: newUsername } = await apiLogin(username, password);

            // 2. Store the token and role in local storage
            localStorage.setItem(TOKEN_KEY, newToken);
            localStorage.setItem(ROLE_KEY, newRole);
            localStorage.setItem(ID_KEY, newId);
            localStorage.setItem(USERNAME_KEY, newUsername);

            // 3. Update the state
            setToken(newToken);
            setRole(newRole);
            setUserId(newId);
            setUserUsername(newUsername);

            return newRole; // Return role for redirect logic in the component
        } catch (error) {
            // Re-throw the error so the component can display a message
            throw error;
        }
    };

    const logout = () => {
        // Clear all keys from local storage
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(ROLE_KEY);
        localStorage.removeItem(ID_KEY);           
        localStorage.removeItem(USERNAME_KEY);     

        // Reset all state
        setToken(null);
        setRole(null);
        setUserId(null);           
        setUserUsername(null);     
    };

    const value = {
        token,
        role,
        userId,        
        userUsername,  
        isAuthenticated,
        login,
        logout,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};