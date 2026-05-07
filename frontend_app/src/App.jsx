import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, useNavigate, Navigate } from 'react-router-dom';

// Core Auth Imports
import { AuthProvider, useAuth } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import { configureFetchWrapper } from './utils/fetchWrapper';
import ProtectedRoute from './routes/ProtectedRoute';

// Pages
import LoginPage from './pages/LoginPage';
import SignUpPage from './pages/SignUpPage';
import HomePage from './pages/HomePage';
import AdminPage from './pages/AdminPage.jsx';
import BuyerPage from './pages/BuyerPage.jsx';
import SellerPage from './pages/SellerPage.jsx';
import NotFoundPage from './pages/NotFoundPage';
import ForgotPasswordEmailPage from './pages/ForgotPasswordEmailPage.jsx';
import ResetPasswordPage from './pages/ResetPasswordPage.jsx';
import VerificationPage from './pages/VerificationPage.jsx';
import ReactivatePage from "./pages/ReactivatePage.jsx";
import SellerProductsPage from "./pages/SellerProductsPage.jsx";
import AdminCategoriesPage from "./pages/AdminCategoriesPage.jsx";

// Import Bootstrap CSS
import 'bootstrap/dist/css/bootstrap.min.css';
import BrowsePage from "./pages/BrowsePage.jsx";

// Component to configure the fetchWrapper with imperative functions
const AppConfigurator = ({ children }) => {
    const { logout, getRefreshToken, setTokens } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        configureFetchWrapper(logout, navigate, getRefreshToken, setTokens);
    }, [logout, navigate, getRefreshToken, setTokens]);

    return children;
};

// Public-only route wrapper
const PublicOnly = ({ children }) => {
    const { isAuthenticated } = useAuth();
    if (isAuthenticated) return <Navigate to="/" replace />;
    return children;
};

const App = () => (
    <BrowserRouter>
        <AuthProvider>
            <NotificationProvider>
            <AppConfigurator>
                <Routes>
                    {/* PUBLIC ROUTES */}
                    <Route path="/" element={<HomePage />} />
                    <Route path="/home" element={<HomePage />} />

                    <Route path="/login" element={<PublicOnly><LoginPage /></PublicOnly>} />
                    <Route path="/register" element={<PublicOnly><SignUpPage /></PublicOnly>} />
                    <Route path="/verify" element={<VerificationPage />} />
                    <Route path="/forgot-password" element={<ForgotPasswordEmailPage />} />
                    <Route path="/reset-password" element={<ResetPasswordPage />} />
                    <Route path="/reactivate-account" element={<ReactivatePage />} />

                    {/* PROTECTED ROUTES */}
                    <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
                        <Route path="/admin" element={<AdminPage />} />
                        <Route path="/admin/categories" element={<AdminCategoriesPage />} />
                    </Route>

                    <Route element={<ProtectedRoute allowedRoles={["BUYER"]} />}>
                        <Route path="/buyer" element={<BuyerPage />} />
                    </Route>

                    <Route element={<ProtectedRoute allowedRoles={["SELLER"]} />}>
                        <Route path="/seller" element={<SellerPage />} />
                        <Route path="/seller/products" element={<SellerProductsPage />} />
                    </Route>

                    <Route element={<ProtectedRoute allowedRoles={['BUYER', 'SELLER', 'ADMIN']} />}>
                        <Route path="/browse" element={<BrowsePage />} />
                    </Route>

                    {/* 404 */}
                    <Route path="*" element={<NotFoundPage />} />
                </Routes>
            </AppConfigurator>
            </NotificationProvider>
        </AuthProvider>
    </BrowserRouter>
);

export default App;