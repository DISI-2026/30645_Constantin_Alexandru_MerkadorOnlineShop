import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, useNavigate } from 'react-router-dom';
// Core Auth Imports
import { AuthProvider, useAuth } from './context/AuthContext';
import { configureFetchWrapper } from './utils/fetchWrapper';
import ProtectedRoute from './routes/ProtectedRoute';

import LoginPage from './pages/LoginPage';
import SignUpPage from './pages/SignUpPage';
import HomePage from './pages/HomePage';
import AdminPage from './pages/AdminPage.jsx';
import ClientPage from './pages/ClientPage';
import NotFoundPage from './pages/NotFoundPage';
// Import Bootstrap CSS
import 'bootstrap/dist/css/bootstrap.min.css';

// Component to configure the fetchWrapper with imperative functions (logout/navigate)
// This is necessary to handle the 401 redirect logic outside of a React component's render cycle.
const AppConfigurator = ({ children }) => {
    const { logout } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        // This links the 401 interceptor logic to the React Router/Auth Context
        configureFetchWrapper(logout, navigate);
    }, [logout, navigate]);

    return children;
};

const App = () => (
    <BrowserRouter>
        <AuthProvider>
            <AppConfigurator>
                <Routes>
                    {/* 1. PUBLIC ROUTES*/}
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignUpPage />} />

                    {/* 2. PROTECTED ROUTES GROUP */}

                    {/* 2a. Default Protected Route (Accessible by both ADMIN and CLIENT) */}
                    {/* Redirects unauthorized users to /login */}
                    <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'CLIENT']} />}>
                        {/* Landing page after successful login */}
                        <Route path="/" element={<HomePage />} />
                    </Route>

                    {/* 2b. Admin-only Routes */}
                    <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                        <Route path="/admin" element={<AdminPage />} />
                    </Route>

                    {/* 2c. Client-only Routes */}
                    <Route element={<ProtectedRoute allowedRoles={['CLIENT']} />}>
                        <Route path="/client" element={<ClientPage />} />
                    </Route>

                    {/* 3. FALLBACK ROUTE: 404/Not Found (Should always be the last route) */}
                    <Route path="*" element={<NotFoundPage />} />

                </Routes>
            </AppConfigurator>
        </AuthProvider>
    </BrowserRouter>
);

export default App;
