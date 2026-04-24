// src/components/ProtectedRoute.jsx

import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * A component to guard routes based on authentication and role.
 * @param {{ allowedRoles: string[] }} props - Array of roles allowed to access the route.
 */
const ProtectedRoute = ({ allowedRoles = [] }) => {
  const { isAuthenticated, role } = useAuth();

  // 1. Check Authentication Status
  if (!isAuthenticated) {
    // User is not logged in: Redirect them to the login page.
    return <Navigate to="/login" replace />;
  }

  // 2. Check Role-Based Access Control (RBAC)
  // If allowedRoles is empty, assume any authenticated user can access.
  const isAuthorized = allowedRoles.length === 0 || allowedRoles.includes(role);

  if (!isAuthorized) {
    // User is logged in but does not have the required role:
    // Redirect them to an Unauthorized page or their own dashboard.
    console.warn(`User role "${role}" is not authorized for this page.`);

    // Choose the appropriate redirect for unauthorized access:
    // Option A: Specific Unauthorized Page
    // return <Navigate to="/unauthorized" replace />;

    // Option B: Redirect back to their own dashboard
    // if (role === 'ADMIN') return <Navigate to="/admin" replace />;
    // if (role === 'CLIENT') return <Navigate to="/client" replace />;

    // Option C: Redirect to a generic dashboard
    // Default fallback
    return <Navigate to="/" replace />;
  }

  // 3. Authorized: Render the nested route component.
  return <Outlet />;
};

export default ProtectedRoute;