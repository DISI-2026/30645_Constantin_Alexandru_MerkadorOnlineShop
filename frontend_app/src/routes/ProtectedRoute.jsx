// src/components/ProtectedRoute.jsx

import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * A component to guard routes based on authentication and role.
 * @param {{ allowedRoles: string[] }} props - Array of roles allowed to access the route.
 */
const ProtectedRoute = ({ allowedRoles = [] }) => {
  const { isAuthenticated, activeRole } = useAuth();

  // 1. Check Authentication Status
  if (!isAuthenticated) {
    // User is not logged in: Redirect them to the login page.
    return <Navigate to="/login" replace />;
  }

  // 2. Check Role-Based Access Control (RBAC) using activeRole (CLIENT/ADMIN)
  const isAuthorized = allowedRoles.length === 0 || allowedRoles.includes(activeRole);

  if (!isAuthorized) {
    console.warn(`User role "${activeRole}" is not authorized for this page.`);
    // Redirect to Home per spec
    return <Navigate to="/" replace />;
  }

  // 3. Authorized: Render the nested route component.
  return <Outlet />;
};

export default ProtectedRoute;