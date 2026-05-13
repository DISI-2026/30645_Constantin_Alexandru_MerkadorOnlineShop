import React, {useState, useEffect, useRef, useCallback} from 'react';
import { Link } from 'react-router-dom';
import MainNavbar from '../components/MainNavbar.jsx';
import VerificationModal from '../components/VerificationModal.jsx';
import { useAuth } from '../context/AuthContext';
import { getSellerProfile, createSellerProfile, uploadLogo } from '../api/userService';
import { categoryService } from '../api/categoryService';
import { sendVerificationRequest as sendVerificationRequestAPI } from '../api/notificationService';
import '../styles/SellerPage.css';
import {orderService} from "../api/orderService";

const SellerPage = () => {
    const { userId } = useAuth();
    const [tab, setTab] = useState('shop');

    const [profile, setProfile] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [uploadingLogo, setUploadingLogo] = useState(false);
    const [showVerificationModal, setShowVerificationModal] = useState(false);
    const [globalCategories, setGlobalCategories] = useState([]);

    const [orders, setOrders] = useState([]);
    const [loadingOrders, setLoadingOrders] = useState(false);
    // --- State for Order Details Modal ---
    const [selectedOrder, setSelectedOrder] = useState(null);


    const isNewProfile = !loading && !profile;
    const fileInputRef = useRef(null);

    const [form, setForm] = useState({
        shopName: '',
        shopSlug: '',
        description: ''
    });

    const loadData = useCallback(async () => {
        try {
            setLoading(true);
            const catRes = await categoryService.getCategories();
            setGlobalCategories(catRes.data || catRes || []);

            const profileRes = await getSellerProfile(userId);
            const profileData = profileRes.data || profileRes;
            setProfile(profileData);
            setForm({
                shopName: profileData.shopName || '',
                shopSlug: profileData.shopSlug || '',
                description: profileData.description || ''
            });
        } catch (error) {
            if (error?.response?.status === 404) {
                setProfile(null); // explicit, though it's already null
            } else {
                console.error("Failed to load profile", error);
            }
        } finally {
            setLoading(false);
        }
    }, [userId]);

    const fetchOrders = useCallback(async () => {
        try {
            setLoadingOrders(true);
            const response = await orderService.getOrdersForSeller();
            setOrders(response.data || response || []);
        } catch (error) {
            console.error("Error fetching orders:", error);
        } finally {
            setLoadingOrders(false);
        }
    }, []);

    useEffect(() => {
        if (userId) loadData();
        if(tab === 'orders')
            fetchOrders();
    }, [loadData, userId, tab, fetchOrders]);

    useEffect(() => {
        if (isNewProfile) setIsEditing(true);
    }, [isNewProfile]);

    const handleInputChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const generateSlug = () => {
        if (!form.shopName) return;
        const slug = form.shopName.toLowerCase().trim()
            .replace(/[ăâ]/g, 'a').replace(/[î]/g, 'i').replace(/[șş]/g, 's').replace(/[țţ]/g, 't')
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-+|-+$/g, '');
        setForm(prev => ({ ...prev, shopSlug: slug }));
    };

    const handleSaveProfile = async () => {
        if (!form.shopName || !form.description) {
            alert("Shop Name and Description are required!");
            return;
        }
        const slugRegex = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
        if (!slugRegex.test(form.shopSlug)) {
            alert("Slug must be lowercase, alphanumeric, and hyphen-separated (e.g. my-shop-name)");
            return;
        }

        try {
            await createSellerProfile(userId, form);
            alert(isNewProfile ? "Shop created successfully!" : "Shop updated successfully!");
            setIsEditing(false);
            await loadData(); // profile will now be non-null, so isNewProfile flips automatically
        } catch (error) {
            console.error("Failed to save profile", error);
            alert("Could not save shop profile.");
        }
    };

    const handleLogoChange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        try {
            setUploadingLogo(true);
            await uploadLogo(userId, file);
            await loadData();
            alert("Logo updated!");
        } catch (error) {
            console.error("Failed to upload logo", error);
            alert("Could not upload logo.");
        } finally {
            setUploadingLogo(false);
            e.target.value = null;
        }
    };

    const handleViewOrderDetails = async (orderId) => {
        try {
            const response = await orderService.getOrderById(orderId);
            setSelectedOrder(response.data || response);
        } catch (error) {
            alert("Failed to load order details.");
            console.error(error);
        }
    };

    const handleStatusChange = async (orderId, newStatus) => {
        try {
            await orderService.updateOrderStatusSeller(orderId, newStatus);
            alert(`Order status updated to ${newStatus}`);
            fetchOrders();
        } catch (error) {
            alert("Failed to update status: " + error.message);
        }
    };

    const closeOrderModal = () => {
        setSelectedOrder(null);
    };

    const getLogoUrl = (url) => {
        if (!url) return 'https://via.placeholder.com/150?text=No+Logo';
        if (url.startsWith('http')) return url;
        if (url.startsWith('/api')) return url;
        return `/api${url.startsWith('/') ? '' : '/'}${url}`;
    };

    const sendVerificationRequest = async (selectedCats, message) => {
        if (selectedCats.length === 0) {
            alert("Please select at least one category.");
            return;
        }

        try {
            // Send the verification request to the administrators
            await sendVerificationRequestAPI(
                profile.shopName,
                selectedCats,
                message
            );

            alert("Verification request sent. Please wait for approval.");
            setShowVerificationModal(false);
        } catch (error) {
            console.error("Failed to send verification request:", error);
            alert("Could not send the request. Please try again later.");
        }
    };

    const tabStyle = (isActive) => ({
        padding: '1rem 2rem', border: 'none', background: 'transparent', cursor: 'pointer',
        borderBottom: isActive ? '3px solid #e67e22' : '3px solid transparent',
        fontWeight: isActive ? 700 : 500, fontSize: '1.1rem',
        color: isActive ? '#d35400' : '#6c757d', transition: 'all 0.2s ease',
    });

    if (loading) return <div style={{ textAlign: 'center', marginTop: '50px' }}>Loading...</div>;

    return (
        <div className="seller-page">
            <div className="seller-page-container">
                <MainNavbar pageTitle="Seller Dashboard" />

                {showVerificationModal && (
                    <VerificationModal
                        categories={globalCategories}
                        onClose={() => setShowVerificationModal(false)}
                        onSend={sendVerificationRequest}
                    />
                )}

                <section className="seller-page-hero">
                    <div>
                        <span className="seller-page-badge">Seller mode</span>
                        <h1>Manage your shop</h1>
                        <p>From here, you can manage your business profile, listed products, stock, prices, images, and incoming customer orders.</p>
                    </div>

                    {!isNewProfile && (
                        <Link to="/seller/products" className="seller-page-primary-btn">
                            Manage products
                        </Link>
                    )}
                </section>

                <div className="seller-main-card">
                    <div className="seller-tabs">
                        <button style={tabStyle(tab === 'shop')} onClick={() => setTab('shop')}>Shop Profile</button>
                        <button style={tabStyle(tab === 'orders')} onClick={() => setTab('orders')} disabled={isNewProfile}>Orders Dashboard</button>
                    </div>

                    {tab === 'shop' && (
                        <div className="seller-tab-content">
                            {isNewProfile && (
                                <div style={{ backgroundColor: '#fff3cd', color: '#856404', padding: '15px', borderRadius: '5px', marginBottom: '20px' }}>
                                    <strong>Welcome!</strong> Please complete your shop profile below to unlock the ability to add and manage products.
                                </div>
                            )}

                            <div className="seller-profile-card">
                                <div className="seller-avatar-section">
                                    <div
                                        className="seller-avatar-wrapper"
                                        onClick={() => (!isNewProfile || isEditing) && fileInputRef.current.click()}
                                        style={{ cursor: (!isNewProfile || isEditing) ? 'pointer' : 'default' }}
                                    >
                                        <img src={getLogoUrl(profile?.logoUrl)} alt="Shop Logo" className="seller-avatar-img" />
                                        {(!isNewProfile || isEditing) && (
                                            <div className="seller-avatar-overlay">
                                                {uploadingLogo ? 'Uploading...' : 'Change Logo'}
                                            </div>
                                        )}
                                    </div>
                                    <input
                                        type="file"
                                        ref={fileInputRef}
                                        style={{ display: 'none' }}
                                        accept="image/jpeg,image/png,image/webp"
                                        onChange={handleLogoChange}
                                    />

                                    {!isNewProfile && (
                                        <div style={{ marginTop: '1rem', textAlign: 'center' }}>
                                            <h3 style={{ margin: 0, color: '#2c3e50' }}>{profile?.shopName}</h3>
                                            <p style={{ color: '#7f8c8d', fontSize: '0.9rem', marginTop: '5px' }}>@{profile?.shopSlug ?? 'shop_slug'}</p>

                                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', marginTop: '10px' }}>
                                                <span style={{
                                                    backgroundColor: profile?.verified ? '#d4edda' : '#f8d7da',
                                                    color: profile?.verified ? '#155724' : '#721c24',
                                                    padding: '3px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold'
                                                }}>
                                                    {profile?.verified ? '✓ Verified' : 'Unverified'}
                                                </span>
                                                {!profile?.verified && (
                                                    <button
                                                        onClick={() => setShowVerificationModal(true)}
                                                        style={{ fontSize: '0.8rem', padding: '3px 8px', background: '#f39c12', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                                                    >
                                                        Request
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </div>

                                <div className="seller-info-section">
                                    <div className="seller-info-header">
                                        <h2 className="seller-info-title">Shop Details</h2>
                                        <div style={{ display: 'flex', gap: '8px' }}>
                                            {!isEditing && !isNewProfile && (
                                                <>
                                                    <button className="seller-edit-btn" onClick={() => setIsEditing(true)}>
                                                        Edit Profile
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                    </div>

                                    {isEditing ? (
                                        <div className="seller-form-grid">
                                            <div className="seller-form-group">
                                                <label>Shop Name *</label>
                                                <input name="shopName" value={form.shopName} onChange={handleInputChange} placeholder="Your awesome shop" />
                                            </div>
                                            <div className="seller-form-group">
                                                <label>Shop Slug</label>
                                                <div style={{ display: 'flex', gap: '10px' }}>
                                                    <input name="shopSlug" value={form.shopSlug} onChange={handleInputChange} placeholder="your-shop-slug" style={{ flex: 1 }} />
                                                    <button type="button" onClick={generateSlug} style={{ padding: '0 15px', background: 'rgb(62,62,62)', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Generate</button>
                                                </div>
                                            </div>
                                            <div className="seller-form-group" style={{ gridColumn: '1 / -1' }}>
                                                <label>Description *</label>
                                                <textarea name="description" value={form.description} onChange={handleInputChange} placeholder="Tell buyers about your products..." style={{ width: '100%', minHeight: '100px', padding: '10px', border: '1px solid #ddd', borderRadius: '6px' }} />
                                            </div>

                                            <div className="seller-form-actions" style={{ gridColumn: '1 / -1' }}>
                                                <button className="seller-btn-primary" onClick={handleSaveProfile}>Save Changes</button>
                                                {!isNewProfile && (
                                                    <button className="seller-btn-secondary" onClick={() => { setIsEditing(false); setForm({ shopName: profile?.shopName || '', shopSlug: profile?.shopSlug || '', description: profile?.description || '' }); }}>Cancel</button>
                                                )}
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="seller-details-grid">
                                            <div className="seller-detail-item">
                                                <span className="seller-detail-label">Average Rating</span>
                                                <span className="seller-detail-value">⭐ {profile?.avgRating ? profile.avgRating.toFixed(1) : 'No ratings'}</span>
                                            </div>
                                            <div className="seller-detail-item">
                                                <span className="seller-detail-label">Total Sales</span>
                                                <span className="seller-detail-value">{profile?.totalSales || 0} {profile?.totalSales > 0 ? 'RON' : ''}</span>
                                            </div>
                                            <div className="seller-detail-item">
                                                <span className="seller-detail-label">Member Since</span>
                                                <span className="seller-detail-value">{profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : 'N/A'}</span>
                                            </div>
                                            <div className="seller-detail-item">
                                                <span className="seller-detail-label">Last Updated</span>
                                                <span className="seller-detail-value">{profile?.updatedAt ? new Date(profile.updatedAt).toLocaleDateString() : 'N/A'}</span>
                                            </div>

                                            <div className="seller-detail-item" style={{ gridColumn: '1 / -1' }}>
                                                <span className="seller-detail-label">Description</span>
                                                <span className="seller-detail-value" style={{ whiteSpace: 'pre-line' }}>{profile?.description || 'No description provided.'}</span>
                                            </div>

                                            <div className="seller-detail-item" style={{ gridColumn: '1 / -1' }}>
                                                <span className="seller-detail-label">Authorized Categories</span>
                                                <div className="seller-categories-container">
                                                    {profile?.authorizedCategories?.length > 0 ? (
                                                        profile.authorizedCategories.map((cat, idx) => (
                                                            <span key={idx} className="seller-category-tag">{cat}</span>
                                                        ))
                                                    ) : (
                                                        <span style={{ color: '#95a5a6', fontStyle: 'italic' }}>This account is not verified. No subset of categories is assigned yet.</span>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    {tab === 'orders' && (
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
                                            <th>Actions</th>
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
                                                    <div className="d-flex gap-2">
                                                        <button
                                                            className="btn btn-sm btn-outline-primary"
                                                            onClick={() => handleViewOrderDetails(order.id)}
                                                        >
                                                            View
                                                        </button>
                                                        <select
                                                            className="form-select form-select-sm"
                                                            style={{ width: 'auto' }}
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
                                                    </div>
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

            {/* ORDER DETAILS MODAL */}
            {selectedOrder && (
                <div className="modal" style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }} tabIndex="-1">
                    <div className="modal-dialog modal-lg">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">Order Details</h5>
                                <button type="button" className="btn-close" onClick={closeOrderModal}></button>
                            </div>
                            <div className="modal-body">
                                <div className="mb-3">
                                    <strong>Order ID:</strong> {selectedOrder.id} <br/>
                                    <strong>Customer ID:</strong> {selectedOrder.customerId} <br/>
                                    <strong>Status:</strong> <span className={`badge bg-${selectedOrder.status === 'PENDING' ? 'warning' : selectedOrder.status === 'CANCELLED' ? 'danger' : 'success'}`}>{selectedOrder.status}</span> <br/>
                                    <strong>Date:</strong> {new Date(selectedOrder.placedAt).toLocaleString()} <br/>
                                    <strong>Delivery Address:</strong> {selectedOrder.deliveryAddress}
                                </div>

                                <h6>Items:</h6>
                                <table className="table table-sm table-striped">
                                    <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>Price</th>
                                        <th>Qty</th>
                                        <th>Subtotal</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {selectedOrder.items?.map(item => (
                                        <tr key={item.productId}>
                                            <td>{item.productTitle} <br/><small className="text-muted">{item.productId}</small></td>
                                            <td>${item.unitPrice.toFixed(2)}</td>
                                            <td>{item.quantity}</td>
                                            <td>${(item.unitPrice * item.quantity).toFixed(2)}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>

                                <h5 className="text-end mt-3">Total: ${selectedOrder.totalAmount?.toFixed(2)}</h5>

                                {selectedOrder.statusHistory && selectedOrder.statusHistory.length > 0 && (
                                    <div className="mt-4">
                                        <h6>Status History:</h6>
                                        <ul className="list-group list-group-flush">
                                            {selectedOrder.statusHistory.map((history, idx) => (
                                                <li key={idx} className="list-group-item py-1 px-2" style={{ fontSize: '0.9rem' }}>
                                                    {new Date(history.changedAt).toLocaleString()} - <strong>{history.fromStatus || 'NEW'} &rarr; {history.toStatus}</strong>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeOrderModal}>Close</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SellerPage;