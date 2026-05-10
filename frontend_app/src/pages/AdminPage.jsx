import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import MainNavbar from '../components/MainNavbar.jsx';
import { orderService } from '../api/orderService';

const AdminPage = () => {
    const [activeTab, setActiveTab] = useState('users');
    const [orders, setOrders] = useState([]);
    const [loadingOrders, setLoadingOrders] = useState(false);

    const tabStyle = (isActive) => ({
        padding: '1rem 2rem',
        border: 'none',
        borderBottom: isActive ? '3px solid #0d6efd' : '3px solid transparent',
        background: 'transparent',
        cursor: 'pointer',
        fontWeight: isActive ? 700 : 500,
        fontSize: '1.1rem',
        color: isActive ? '#0d6efd' : '#6c757d',
        transition: 'all 0.2s ease',
    });

    const fetchOrders = useCallback(async () => {
        try {
            setLoadingOrders(true);
            const response = await orderService.getAllOrdersAdmin();
            setOrders(response.data || response || []);
        } catch (error) {
            console.error("Error fetching orders:", error);
        } finally {
            setLoadingOrders(false);
        }
    }, []);

    useEffect(() => {
        if (activeTab === 'orders') {
            fetchOrders();
        }
    }, [activeTab, fetchOrders]);

    const handleStatusChange = async (orderId, newStatus) => {
        try {
            await orderService.updateOrderStatus(orderId, newStatus);
            alert(`Order status updated to ${newStatus}`);
            fetchOrders();
        } catch (error) {
            alert("Failed to update status: " + error.message);
        }
    };

    return (
        <div style={{ minHeight: '100vh', width: '100vw', backgroundColor: '#f4f6f8', margin: 0, padding: 0, color: '#333', textAlign: 'left' }}>
            <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '2rem 5%' }}>

                <MainNavbar pageTitle="Admin Control Panel" />

                <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', boxShadow: '0 8px 24px rgba(0,0,0,0.05)', padding: '2rem', marginTop: '1rem', minHeight: '60vh' }}>

                    <div style={{ borderBottom: '1px solid #e1e1e1', marginBottom: '2rem', display: 'flex', gap: '1rem' }}>
                        <button style={tabStyle(activeTab === 'users')} onClick={() => setActiveTab('users')}>
                            User Dashboard
                        </button>

                        <button style={tabStyle(activeTab === 'products')} onClick={() => setActiveTab('products')}>
                            Product Dashboard
                        </button>

                        <button style={tabStyle(activeTab === 'orders')} onClick={() => setActiveTab('orders')}>
                            Orders Dashboard
                        </button>
                    </div>

                    {activeTab === 'users' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>User Dashboard</h2>

                            <p style={{ color: '#555', fontSize: '1.05rem' }}>
                                Here the admin will see and manage users, approve seller profiles, ban users and monitor platform activity.
                            </p>
                        </div>
                    )}

                    {activeTab === 'products' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Product Dashboard</h2>

                            <p style={{ color: '#555', fontSize: '1.05rem' }}>
                                Here the admin will manage product categories and the global catalog.
                            </p>

                            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem', flexWrap: 'wrap' }}>
                                <Link
                                    to="/admin/categories"
                                    style={{
                                        display: 'inline-flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        backgroundColor: '#0d6efd',
                                        color: 'white',
                                        padding: '12px 18px',
                                        borderRadius: '8px',
                                        textDecoration: 'none',
                                        fontWeight: 700
                                    }}
                                >
                                    Manage Categories
                                </Link>
                            </div>
                        </div>
                    )}

                    {activeTab === 'orders' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Orders Dashboard</h2>
                            <p style={{ color: '#555', fontSize: '1.05rem', marginBottom: '1.5rem' }}>
                                Manage all orders across the platform. Update order statuses here.
                            </p>

                            {loadingOrders ? (
                                <p>Loading orders...</p>
                            ) : orders.length === 0 ? (
                                <p>No orders found.</p>
                            ) : (
                                <div className="table-responsive">
                                    <table className="table table-bordered table-hover">
                                        <thead className="table-light">
                                            <tr>
                                                <th>Order ID</th>
                                                <th>Date</th>
                                                <th>Total Amount</th>
                                                <th>Customer ID</th>
                                                <th>Delivery Address</th>
                                                <th>Current Status</th>
                                                <th>Update Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {orders.map(order => (
                                                <tr key={order.id}>
                                                    <td><small>{order.id}</small></td>
                                                    <td>{new Date(order.placedAt).toLocaleString()}</td>
                                                    <td>${order.totalAmount?.toFixed(2)}</td>
                                                    <td><small>{order.customerId}</small></td>
                                                    <td>{order.deliveryAddress}</td>
                                                    <td>
                                                        <span className={`badge bg-${order.status === 'PENDING' ? 'warning' : order.status === 'CANCELLED' ? 'danger' : 'success'}`}>
                                                            {order.status}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <select 
                                                            className="form-select form-select-sm"
                                                            value={order.status}
                                                            onChange={(e) => handleStatusChange(order.id, e.target.value)}
                                                            disabled={order.status === 'CANCELLED' || order.status === 'DELIVERED'}
                                                        >
                                                            <option value="PENDING">PENDING</option>
                                                            <option value="PROCESSING">PROCESSING</option>
                                                            <option value="SHIPPED">SHIPPED</option>
                                                            <option value="DELIVERED">DELIVERED</option>
                                                            <option value="CANCELLED">CANCELLED</option>
                                                        </select>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminPage;
