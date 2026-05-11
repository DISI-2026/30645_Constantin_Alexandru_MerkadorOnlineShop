// src/pages/BuyerPage.jsx
import React, { useState, useEffect, useCallback, useRef } from 'react';
import MainNavbar from '../components/MainNavbar.jsx';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { deactivate, changePassword, requestEmailUpdate } from '../api/credentialsService';
import { categoryService } from '../api/categoryService';
import { orderService } from '../api/orderService';
import {
    getUserById,
    updateUser,
    getUserAddresses,
    addAddress,
    setDefaultAddress,
    deleteAddress,
    uploadAvatar
} from '../api/userService';
import '../styles/BuyerPage.css';

const BuyerPage = () => {
    const { userId, email: currentEmail, logout } = useAuth();
    const navigate = useNavigate();
    const [tab, setTab] = useState('account');

    // --- State for Categories ---
    const [availableCategories, setAvailableCategories] = useState([]);

    // --- State for Profile Data ---
    const [profile, setProfile] = useState(null);
    const [isEditingProfile, setIsEditingProfile] = useState(false);
    const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
    const [profileForm, setProfileForm] = useState({
        firstName: '',
        lastName: '',
        phone: '',
        preferredCategories: []
    });

    const fileInputRef = useRef(null);

    const [isChangingEmail, setIsChangingEmail] = useState(false);
    const [newEmail, setNewEmail] = useState('');

    const [isChangingPassword, setIsChangingPassword] = useState(false);
    const [passwordForm, setPasswordForm] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
    });

    const [addresses, setAddresses] = useState([]);
    const [showAddAddress, setShowAddAddress] = useState(false);
    const [addressForm, setAddressForm] = useState({
        label: '',
        addressLine: '',
        city: '',
        country: '',
        postalCode: '',
        isDefault: false
    });

    // --- State for Orders ---
    const [orders, setOrders] = useState([]);
    const [loadingOrders, setLoadingOrders] = useState(false);
    
    // --- State for Order Details Modal ---
    const [selectedOrder, setSelectedOrder] = useState(null);

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const response = await categoryService.getCategories();
                const categories = response.data || [];
                setAvailableCategories(categories || []);
            } catch (error) {
                console.error("Failed to load categories:", error);
            }
        };
        fetchCategories();
    }, []);

    const fetchUserData = useCallback(async () => {
        if (!userId) return;
        try {
            const userProfile = await getUserById(userId);
            setProfile(userProfile);
            setProfileForm({
                firstName: userProfile.firstName || '',
                lastName: userProfile.lastName || '',
                phone: userProfile.phone || '',
                preferredCategories: userProfile.preferredCategories || []
            });

            const userAddresses = await getUserAddresses(userId);
            setAddresses(userAddresses || []);
        } catch (error) {
            console.error("Error fetching user data:", error);
        }
    }, [userId]);

    const fetchOrders = useCallback(async () => {
        try {
            setLoadingOrders(true);
            const response = await orderService.getOrderHistory();
            setOrders(response.data || response || []);
        } catch (error) {
            console.error("Error fetching orders:", error);
        } finally {
            setLoadingOrders(false);
        }
    }, []);

    useEffect(() => {
        fetchUserData();
    }, [fetchUserData]);

    useEffect(() => {
        if (tab === 'orders') {
            fetchOrders();
        }
    }, [tab, fetchOrders]);

    // ==========================================
    // PROFILE HANDLERS
    // ==========================================
    const handleProfileChange = (e) => {
        const { name, value } = e.target;
        setProfileForm(prev => ({ ...prev, [name]: value }));
    };

    const handleCategoryToggle = (slug) => {
        setProfileForm(prev => {
            const isAlreadySelected = prev.preferredCategories.includes(slug);
            const updatedCategories = isAlreadySelected
                ? prev.preferredCategories.filter(catSlug => catSlug !== slug)
                : [...prev.preferredCategories, slug];

            return { ...prev, preferredCategories: updatedCategories };
        });
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        try {
            await updateUser(userId, profileForm);
            setIsEditingProfile(false);
            fetchUserData();
        } catch (error) {
            alert("Failed to update profile: " + error.message);
        }
    };

    // ==========================================
    // SENSITIVE CREDENTIAL HANDLERS
    // ==========================================
    const handleEmailUpdateRequest = async () => {
        if (!newEmail || newEmail === currentEmail) {
            return alert("Please enter a valid, new email address.");
        }
        try {
            await requestEmailUpdate(userId, newEmail);
            navigate(`/verify?email=${encodeURIComponent(newEmail)}&type=emailUpdate&userId=${userId}`);
        } catch (error) {
            alert("Failed to request email update: " + error.message);
        }
    };

    const handlePasswordChange = async () => {
        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            return alert("New passwords do not match.");
        }
        try {
            await changePassword(userId, passwordForm.oldPassword, passwordForm.newPassword);
            alert("Password updated successfully!");
            setIsChangingPassword(false);
            setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
        } catch (error) {
            alert("Failed to change password: " + error.message);
        }
    };

    const handleDeactivateAccount = async () => {
        if (window.confirm("Are you sure you want to deactivate your account? You can choose to reactivate it later.")) {
            try {
                await deactivate(userId);
                await logout();
                navigate('/login');
            } catch (error) {
                alert("Failed to deactivate account: " + error.message);
            }
        }
    };

    const handleAvatarClick = () => {
        if (!isUploadingAvatar && fileInputRef.current) {
            fileInputRef.current.click();
        }
    };

    const handleFileChange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            alert("Please select a valid image file.");
            return;
        }

        setIsUploadingAvatar(true);
        try {
            await uploadAvatar(userId, file);
            await fetchUserData();
        } catch (error) {
            console.error("Avatar upload failed:", error);
            alert("Failed to upload avatar: " + error.message);
        } finally {
            setIsUploadingAvatar(false);
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    };

    // ==========================================
    // ADDRESS HANDLERS
    // ==========================================
    const handleAddressChange = (e) => {
        const { name, value, type, checked } = e.target;
        setAddressForm(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleAddressSubmit = async (e) => {
        e.preventDefault();
        try {
            await addAddress(userId, addressForm);
            setShowAddAddress(false);
            setAddressForm({ label: '', addressLine: '', city: '', country: '', postalCode: '', isDefault: false });
            fetchUserData();
        } catch (error) {
            alert("Failed to add address: " + error.message);
        }
    };

    const handleSetDefault = async (addressId) => {
        try {
            await setDefaultAddress(userId, addressId);
            fetchUserData();
        } catch (error) {
            alert("Failed to set default address: " + error.message);
        }
    };

    const handleDeleteAddress = async (addressId) => {
        if (!window.confirm("Are you sure you want to delete this address?")) return;
        try {
            await deleteAddress(userId, addressId);
            fetchUserData();
        } catch (error) {
            alert("Failed to delete address: " + error.message);
        }
    };

    // ==========================================
    // ORDER HANDLERS
    // ==========================================
    const handleCancelOrder = async (orderId) => {
        if (!window.confirm("Are you sure you want to cancel this order?")) return;
        try {
            await orderService.cancelOrder(orderId);
            alert("Order cancelled successfully.");
            fetchOrders();
        } catch (error) {
            alert("Failed to cancel order: " + error.message);
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

    const closeOrderModal = () => {
        setSelectedOrder(null);
    };

    return (
        <div className="buyer-page-wrapper">
            <div className="buyer-page-container">
                <MainNavbar pageTitle="Buyer Dashboard" />

                <div className="buyer-content-card">
                    {/* Tabs Navigation */}
                    <div className="buyer-tabs-container">
                        <button className={`buyer-tab-btn ${tab === 'account' ? 'active' : ''}`} onClick={() => setTab('account')}>Account</button>
                        <button className={`buyer-tab-btn ${tab === 'orders' ? 'active' : ''}`} onClick={() => setTab('orders')}>My Orders</button>
                    </div>

                    {/* ACCOUNT TAB CONTENT */}
                    {tab === 'account' && profile && (
                        <div className="row">
                            {/* Left Column: Personal Info */}
                            <div className="col-md-5 mb-4">
                                <div className="card shadow-sm border-0 h-100">
                                    <div className="card-body">
                                        <h4 className="card-title mb-4">Personal Information</h4>

                                        <div className="avatar-container" onClick={handleAvatarClick} title="Click to change avatar">
                                            {isUploadingAvatar ? (
                                                <div className="spinner-border text-success" role="status"><span className="visually-hidden">Loading...</span></div>
                                            ) : profile.avatarUrl ? (
                                                <><img src={profile.avatarUrl} alt="Avatar" className="avatar-image" /><div className="avatar-overlay">Change</div></>
                                            ) : (
                                                <><span className="text-muted">No Avatar</span><div className="avatar-overlay">Upload</div></>
                                            )}
                                        </div>
                                        <input type="file" ref={fileInputRef} style={{ display: 'none' }} accept="image/*" onChange={handleFileChange} />

                                        {!isEditingProfile ? (
                                            <div className="mt-4 text-center text-md-start">
                                                <p><strong>First Name:</strong> {profile.firstName}</p>
                                                <p><strong>Last Name:</strong> {profile.lastName}</p>
                                                <p><strong>Phone:</strong> {profile.phone || 'Not set'}</p>

                                                <div className="mt-3 text-start">
                                                    <p className="mb-1"><strong>Preferred Categories:</strong></p>
                                                    <div className="category-tags-container">
                                                        {profile.preferredCategories && profile.preferredCategories.length > 0 ? (
                                                            profile.preferredCategories.map(slug => {
                                                                const catName = availableCategories.find(c => c.slug === slug)?.name || slug;
                                                                return <span key={slug} className="category-tag read-only">{catName}</span>;
                                                            })
                                                        ) : (
                                                            <span className="text-muted small">No preferences set</span>
                                                        )}
                                                    </div>
                                                </div>

                                                <button className="btn btn-outline-success mt-4 w-100" onClick={() => setIsEditingProfile(true)}>
                                                    Edit Profile
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="mt-4 text-start">
                                                <form onSubmit={handleProfileSubmit} className="mb-4 pb-4 border-bottom">
                                                    <h6 className="text-muted mb-3">Basic Info</h6>
                                                    <div className="mb-2">
                                                        <label className="form-label small">First Name</label>
                                                        <input type="text" className="form-control form-control-sm" name="firstName" value={profileForm.firstName} onChange={handleProfileChange} required />
                                                    </div>
                                                    <div className="mb-2">
                                                        <label className="form-label small">Last Name</label>
                                                        <input type="text" className="form-control form-control-sm" name="lastName" value={profileForm.lastName} onChange={handleProfileChange} required />
                                                    </div>
                                                    <div className="mb-3">
                                                        <label className="form-label small">Phone</label>
                                                        <input type="text" className="form-control form-control-sm" name="phone" value={profileForm.phone} onChange={handleProfileChange} />
                                                    </div>

                                                    <div className="mb-4">
                                                        <label className="form-label small">Preferred Categories</label>
                                                        <div className="category-tags-container">
                                                            {availableCategories.length > 0 ? (
                                                                availableCategories.map(cat => {
                                                                    const isSelected = profileForm.preferredCategories.includes(cat.slug);
                                                                    return (
                                                                        <button
                                                                            key={cat.slug}
                                                                            type="button"
                                                                            className={`category-tag toggleable ${isSelected ? 'selected' : ''}`}
                                                                            onClick={() => handleCategoryToggle(cat.slug)}
                                                                        >
                                                                            {cat.name}
                                                                        </button>
                                                                    );
                                                                })
                                                            ) : (
                                                                <span className="text-muted small">Loading categories...</span>
                                                            )}
                                                        </div>
                                                    </div>

                                                    <div className="d-flex gap-2">
                                                        <button type="submit" className="btn btn-success btn-sm flex-grow-1">Save Profile</button>
                                                        <button type="button" className="btn btn-secondary btn-sm flex-grow-1" onClick={() => {
                                                            setIsEditingProfile(false);
                                                            setProfileForm({
                                                                firstName: profile.firstName || '',
                                                                lastName: profile.lastName || '',
                                                                phone: profile.phone || '',
                                                                preferredCategories: profile.preferredCategories || []
                                                            });
                                                        }}>Cancel</button>
                                                    </div>
                                                </form>

                                                <h6 className="text-muted mb-3">Security & Account</h6>

                                                <div className="mb-3 p-3 bg-light border rounded">
                                                    <label className="form-label small fw-bold">Email Address</label>
                                                    {!isChangingEmail ? (
                                                        <div className="d-flex gap-2">
                                                            <input type="text" className="form-control form-control-sm text-muted" value={currentEmail} disabled />
                                                            <button type="button" className="btn btn-outline-primary btn-sm" onClick={() => setIsChangingEmail(true)}>Change</button>
                                                        </div>
                                                    ) : (
                                                        <div>
                                                            <input type="email" className="form-control form-control-sm mb-2" placeholder="Enter new email" value={newEmail} onChange={e => setNewEmail(e.target.value)} />
                                                            <div className="d-flex gap-2">
                                                                <button type="button" className="btn btn-primary btn-sm flex-grow-1" onClick={handleEmailUpdateRequest}>Send Code</button>
                                                                <button type="button" className="btn btn-outline-secondary btn-sm flex-grow-1" onClick={() => { setIsChangingEmail(false); setNewEmail(''); }}>Cancel</button>
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>

                                                <div className="mb-3 p-3 bg-light border rounded">
                                                    {!isChangingPassword ? (
                                                        <button type="button" className="btn btn-outline-warning btn-sm w-100" onClick={() => setIsChangingPassword(true)}>
                                                            Change Password
                                                        </button>
                                                    ) : (
                                                        <div>
                                                            <label className="form-label small fw-bold">Change Password</label>
                                                            <input type="password" className="form-control form-control-sm mb-2" placeholder="Old Password" value={passwordForm.oldPassword} onChange={e => setPasswordForm({...passwordForm, oldPassword: e.target.value})} />
                                                            <input type="password" className="form-control form-control-sm mb-2" placeholder="New Password" value={passwordForm.newPassword} onChange={e => setPasswordForm({...passwordForm, newPassword: e.target.value})} />
                                                            <input type="password" className="form-control form-control-sm mb-3" placeholder="Confirm New Password" value={passwordForm.confirmPassword} onChange={e => setPasswordForm({...passwordForm, confirmPassword: e.target.value})} />
                                                            <div className="d-flex gap-2">
                                                                <button type="button" className="btn btn-warning btn-sm flex-grow-1" onClick={handlePasswordChange}>Update Password</button>
                                                                <button type="button" className="btn btn-outline-secondary btn-sm flex-grow-1" onClick={() => setIsChangingPassword(false)}>Cancel</button>
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>

                                                <div className="mt-4 pt-3 border-top">
                                                    <button type="button" className="btn btn-danger btn-sm w-100 fw-bold" onClick={handleDeactivateAccount}>
                                                        Deactivate Account
                                                    </button>
                                                </div>

                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* Right Column: Address Book */}
                            <div className="col-md-7 mb-4">
                                <div className="card shadow-sm border-0 h-100">
                                    <div className="card-body">
                                        <div className="d-flex justify-content-between align-items-center mb-4">
                                            <h4 className="card-title m-0">Address Book</h4>
                                            <button
                                                className="btn btn-success btn-sm"
                                                onClick={() => setShowAddAddress(!showAddAddress)}
                                            >
                                                {showAddAddress ? 'Cancel' : '+ Add Address'}
                                            </button>
                                        </div>

                                        {showAddAddress && (
                                            <form onSubmit={handleAddressSubmit} className="bg-light p-3 rounded mb-4 border">
                                                <div className="row">
                                                    <div className="col-md-6 mb-2">
                                                        <label className="form-label small">Label (e.g. Home, Work)</label>
                                                        <input type="text" className="form-control form-control-sm" name="label" value={addressForm.label} onChange={handleAddressChange} required />
                                                    </div>
                                                    <div className="col-md-6 mb-2">
                                                        <label className="form-label small">City</label>
                                                        <input type="text" className="form-control form-control-sm" name="city" value={addressForm.city} onChange={handleAddressChange} required />
                                                    </div>
                                                    <div className="col-12 mb-2">
                                                        <label className="form-label small">Address Line</label>
                                                        <input type="text" className="form-control form-control-sm" name="addressLine" value={addressForm.addressLine} onChange={handleAddressChange} required />
                                                    </div>
                                                    <div className="col-md-6 mb-2">
                                                        <label className="form-label small">Country</label>
                                                        <input type="text" className="form-control form-control-sm" name="country" value={addressForm.country} onChange={handleAddressChange} required />
                                                    </div>
                                                    <div className="col-md-6 mb-3">
                                                        <label className="form-label small">Postal Code</label>
                                                        <input type="text" className="form-control form-control-sm" name="postalCode" value={addressForm.postalCode} onChange={handleAddressChange} required />
                                                    </div>
                                                    <div className="col-12 mb-3 form-check ms-2">
                                                        <input type="checkbox" className="form-check-input" id="isDefaultCheck" name="isDefault" checked={addressForm.isDefault} onChange={handleAddressChange} />
                                                        <label className="form-check-label small" htmlFor="isDefaultCheck">Set as default address</label>
                                                    </div>
                                                    <div className="col-12">
                                                        <button type="submit" className="btn btn-success btn-sm w-100">Save Address</button>
                                                    </div>
                                                </div>
                                            </form>
                                        )}

                                        {addresses.length === 0 && !showAddAddress ? (
                                            <p className="text-muted">You have no saved addresses.</p>
                                        ) : (
                                            <div>
                                                {addresses.map(addr => (
                                                    <div key={addr.id} className={`address-card ${addr.isDefault ? 'is-default' : ''}`}>
                                                        {addr.isDefault && <span className="default-badge">Default</span>}
                                                        <h6 className="mb-1">{addr.label}</h6>
                                                        <p className="mb-1 text-muted small">{addr.addressLine}, {addr.city}, {addr.postalCode}, {addr.country}</p>

                                                        <div className="mt-2 d-flex gap-2">
                                                            {!addr.isDefault && (
                                                                <button
                                                                    className="btn btn-outline-success btn-sm"
                                                                    onClick={() => handleSetDefault(addr.id)}
                                                                >
                                                                    Set Default
                                                                </button>
                                                            )}
                                                            <button
                                                                className="btn btn-outline-danger btn-sm"
                                                                onClick={() => handleDeleteAddress(addr.id)}
                                                            >
                                                                Delete
                                                            </button>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {tab === 'orders' && (
                        <div>
                            <h2 style={{ color: '#2c3e50', marginBottom: '1rem' }}>Order History</h2>
                            {loadingOrders ? (
                                <p>Loading orders...</p>
                            ) : orders.length === 0 ? (
                                <p style={{ color: '#555', fontSize: '1.05rem' }}>You haven't placed any orders yet.</p>
                            ) : (
                                <div className="table-responsive">
                                    <table className="table table-bordered table-hover mt-3">
                                        <thead className="table-light">
                                            <tr>
                                                <th>Date</th>
                                                <th>Total Amount</th>
                                                <th>Status</th>
                                                <th>Delivery Address</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {orders.map(order => (
                                                <tr key={order.id}>
                                                    <td>{new Date(order.placedAt).toLocaleString()}</td>
                                                    <td>${order.totalAmount?.toFixed(2)}</td>
                                                    <td>
                                                        <span className={`badge bg-${order.status === 'PENDING' ? 'warning' : order.status === 'CANCELLED' ? 'danger' : 'success'}`}>
                                                            {order.status}
                                                        </span>
                                                    </td>
                                                    <td>{order.deliveryAddress}</td>
                                                    <td>
                                                        <div className="d-flex gap-2">
                                                            <button 
                                                                className="btn btn-sm btn-outline-primary"
                                                                onClick={() => handleViewOrderDetails(order.id)}
                                                            >
                                                                View
                                                            </button>
                                                            {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
                                                                <button 
                                                                    className="btn btn-sm btn-outline-danger"
                                                                    onClick={() => handleCancelOrder(order.id)}
                                                                >
                                                                    Cancel
                                                                </button>
                                                            )}
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

export default BuyerPage;
