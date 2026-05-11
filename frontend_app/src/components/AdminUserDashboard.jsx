import React, { useState, useEffect } from 'react';
import {
    getAllCredentials,
    updateCredentialStatus,
    addCredentialRole,
    deleteCredential
} from '../api/credentialsService';
import {
    getSellers,
    getPendingSellers,
    verifySellerProfile
} from '../api/userService';
import { categoryService } from '../api/categoryService';

const AdminUserDashboard = () => {
    const [activeSubTab, setActiveSubTab] = useState('users'); // 'users' | 'sellers'

    // ==========================================
    // STATE - CATEGORIES
    // ==========================================
    const [availableCategories, setAvailableCategories] = useState([]);

    // ==========================================
    // STATE - USER MANAGEMENT
    // ==========================================
    const [users, setUsers] = useState([]);
    const [searchBy, setSearchBy] = useState('email');
    const [searchQuery, setSearchQuery] = useState('');

    // ==========================================
    // STATE - SELLER MANAGEMENT
    // ==========================================
    const [sellers, setSellers] = useState([]);
    const [sellerFilter, setSellerFilter] = useState('all'); // 'all' | 'pending'

    // Modal State - Authorize Categories
    const [showAuthModal, setShowAuthModal] = useState(false);
    const [selectedSellerForAuth, setSelectedSellerForAuth] = useState(null);
    const [selectedCategories, setSelectedCategories] = useState([]);

    // Modal State - View Description
    const [showDescModal, setShowDescModal] = useState(false);
    const [descSeller, setDescSeller] = useState(null);

    // ==========================================
    // EFFECT HOOKS
    // ==========================================

    // Fetch Categories o singura data la incarcarea componentei
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

    // Fetch Users sau Sellers in functie de tab
    useEffect(() => {
        if (activeSubTab === 'users') {
            fetchUsers();
        } else {
            fetchSellers();
        }
    }, [activeSubTab, sellerFilter]);

    // ==========================================
    // USER MANAGEMENT METHODS
    // ==========================================
    const fetchUsers = async () => {
        try {
            const data = await getAllCredentials();
            setUsers(data || []);
        } catch (error) {
            console.error("Failed to fetch users", error);
        }
    };

    const handlePromoteToAdmin = async (id) => {
        if (window.confirm("Are you sure you want to promote this user to ADMIN?")) {
            try {
                await addCredentialRole(id, 'ADMIN');
                fetchUsers();
            } catch (error) {
                alert("Failed to promote user: " + error.message || "Unknown error occurred.");
            }
        }
    };

    const handleToggleStatus = async (id, currentStatus) => {
        const newStatus = currentStatus === 'SUSPENDED' ? 'PENDING_VERIFICATION' : 'SUSPENDED';
        try {
            await updateCredentialStatus(id, newStatus);
            fetchUsers();
        } catch (error) {
            alert(`Failed to change status to ${newStatus}: ${error.message || "Unknown error occurred."}`);
        }
    };

    const handleDeleteUser = async (user) => {
        // Admins can be deleted only if they are SUSPENDED first
        if (user.roles?.includes('ADMIN') && user.status !== 'SUSPENDED') {
            alert("Error: An ADMIN account must be SUSPENDED before it can be deleted.");
            return;
        }

        if (window.confirm("WARNING: Are you sure you want to DELETE this user? This action is irreversible.")) {
            try {
                await deleteCredential(user.id);
                fetchUsers();
            } catch (error) {
                alert("Failed to delete user: " + error.message || "Unknown error occurred.");
            }
        }
    };

    const filteredUsers = users.filter(u => {
        if (!searchQuery) return true;
        const field = u[searchBy]?.toString().toLowerCase() || '';
        return field.includes(searchQuery.toLowerCase());
    });

    // ==========================================
    // SELLER MANAGEMENT METHODS
    // ==========================================
    const fetchSellers = async () => {
        try {
            const data = sellerFilter === 'pending'
                ? await getPendingSellers()
                : await getSellers();
            setSellers(data || []);
        } catch (error) {
            console.error("Failed to fetch sellers", error);
        }
    };

    // Description Modal methods
    const openDescModal = (seller) => {
        setDescSeller(seller);
        setShowDescModal(true);
    };

    const closeDescModal = () => {
        setShowDescModal(false);
        setDescSeller(null);
    };

    // Authorize Modal methods
    const openAuthorizeModal = (seller) => {
        setSelectedSellerForAuth(seller);
        setSelectedCategories(seller.authorizedCategories ? [...seller.authorizedCategories] : []);
        setShowAuthModal(true);
    };

    const closeAuthorizeModal = () => {
        setShowAuthModal(false);
        setSelectedSellerForAuth(null);
        setSelectedCategories([]);
    };

    const toggleCategory = (catId) => {
        if (selectedCategories.includes(catId)) {
            setSelectedCategories(selectedCategories.filter(c => c !== catId));
        } else {
            setSelectedCategories([...selectedCategories, catId]);
        }
    };

    const handleAuthorizeSeller = async () => {
        if (!selectedSellerForAuth) return;
        try {
            await verifySellerProfile(selectedSellerForAuth.userId, selectedCategories);
            closeAuthorizeModal();
            fetchSellers(); // refresh list
        } catch (error) {
            alert("Failed to verify/authorize seller: " + (error.response?.data?.message || "Unknown error occurred."));
        }
    };

    // Helper for date formatting
    const formatDate = (dateStr) => {
        if (!dateStr) return 'N/A';
        return new Date(dateStr).toLocaleDateString('ro-RO', {
            year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    };

    return (
        <div>
            {/* Sub-tabs Navigation */}
            <ul className="nav nav-pills mb-4">
                <li className="nav-item">
                    <button
                        className={`nav-link ${activeSubTab === 'users' ? 'active' : ''}`}
                        onClick={() => setActiveSubTab('users')}
                        style={{ cursor: 'pointer', borderRadius: '8px', marginRight: '10px' }}
                    >
                        User Management
                    </button>
                </li>
                <li className="nav-item">
                    <button
                        className={`nav-link ${activeSubTab === 'sellers' ? 'active' : ''}`}
                        onClick={() => setActiveSubTab('sellers')}
                        style={{ cursor: 'pointer', borderRadius: '8px' }}
                    >
                        Seller Management
                    </button>
                </li>
            </ul>

            {/* TAB: USER MANAGEMENT */}
            {activeSubTab === 'users' && (
                <div className="fade-in">
                    {/* Filtre de cautare */}
                    <div className="d-flex gap-3 mb-4 align-items-center">
                        <select
                            className="form-select w-auto"
                            value={searchBy}
                            onChange={(e) => setSearchBy(e.target.value)}
                        >
                            <option value="email">Search by Email</option>
                            <option value="id">Search by ID</option>
                        </select>
                        <input
                            type="text"
                            className="form-control w-50"
                            placeholder={`Enter ${searchBy}...`}
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                    </div>

                    {/* Users tabel */}
                    <div className="table-responsive">
                        <table className="table table-hover table-bordered align-middle">
                            <thead className="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Email</th>
                                <th>Status</th>
                                <th>Roles</th>
                                <th>Created At</th>
                                <th>Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {filteredUsers.length > 0 ? filteredUsers.map(user => (
                                <tr key={user.id}>
                                    <td style={{ fontSize: '0.85rem', color: '#666' }}>{user.id}</td>
                                    <td className="fw-bold">{user.email}</td>
                                    <td>
                                            <span className={`badge ${user.status === 'SUSPENDED' ? 'bg-danger' : 'bg-success'}`}>
                                                {user.status}
                                            </span>
                                    </td>
                                    <td>{user.roles?.join(', ')}</td>
                                    <td style={{ fontSize: '0.9rem' }}>{formatDate(user.createdAt)}</td>
                                    <td>
                                        <div className="d-flex gap-2">
                                            <button
                                                className="btn btn-sm btn-outline-primary"
                                                onClick={() => handlePromoteToAdmin(user.id)}
                                                disabled={user.roles?.includes('ADMIN')}
                                            >
                                                Promote
                                            </button>
                                            <button
                                                className={`btn btn-sm ${user.status === 'SUSPENDED' ? 'btn-success' : 'btn-warning'}`}
                                                onClick={() => handleToggleStatus(user.id, user.status)}
                                            >
                                                {user.status === 'SUSPENDED' ? 'Unsuspend' : 'Suspend'}
                                            </button>
                                            <button
                                                className="btn btn-sm btn-danger"
                                                onClick={() => handleDeleteUser(user)}
                                                disabled={user.roles?.includes('ADMIN') && user.status !== 'SUSPENDED'}
                                                title={user.roles?.includes('ADMIN') && user.status !== 'SUSPENDED' ? "Admins must be suspended before deletion" : "Delete User"}
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr><td colSpan="6" className="text-center text-muted py-4">No users found.</td></tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* TAB: SELLER MANAGEMENT */}
            {activeSubTab === 'sellers' && (
                <div className="fade-in">
                    <div className="btn-group mb-4" role="group">
                        <button
                            type="button"
                            className={`btn ${sellerFilter === 'all' ? 'btn-primary' : 'btn-outline-primary'}`}
                            onClick={() => setSellerFilter('all')}
                        >
                            All Sellers
                        </button>
                        <button
                            type="button"
                            className={`btn ${sellerFilter === 'pending' ? 'btn-primary' : 'btn-outline-primary'}`}
                            onClick={() => setSellerFilter('pending')}
                        >
                            Pending Sellers
                        </button>
                    </div>

                    {/* Sellers tabel */}
                    <div className="table-responsive">
                        <table className="table table-hover table-bordered align-middle">
                            <thead className="table-light">
                            <tr>
                                <th>Shop Name</th>
                                <th>Slug</th>
                                <th>Verified</th>
                                <th>Categories</th>
                                <th>Created At</th>
                                <th>Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {sellers.length > 0 ? sellers.map(seller => (
                                <tr key={seller.userId}>
                                    <td className="fw-bold">
                                        {seller.shopName}
                                    </td>
                                    <td>{seller.shopSlug}</td>
                                    <td>
                                            <span className={`badge ${seller.verified ? 'bg-success' : 'bg-secondary'}`}>
                                                {seller.verified ? 'Verified' : 'Pending'}
                                            </span>
                                    </td>
                                    <td>
                                        <div className="d-flex flex-wrap gap-1">
                                            {seller.authorizedCategories?.map(cat => {
                                                // map slug or name to display name
                                                const catObj = availableCategories.find(c => c.slug === cat || c.name === cat);
                                                const displayName = catObj ? catObj.name : cat;

                                                return <span key={cat} className="badge bg-info text-dark">{displayName}</span>
                                            })}
                                        </div>
                                    </td>
                                    <td style={{ fontSize: '0.9rem' }}>{formatDate(seller.createdAt)}</td>
                                    <td>
                                        <div className="d-flex gap-2 flex-wrap">
                                            <button
                                                className="btn btn-sm btn-outline-info"
                                                onClick={() => openDescModal(seller)}
                                            >
                                                View Description
                                            </button>
                                            <button
                                                className="btn btn-sm btn-primary"
                                                onClick={() => openAuthorizeModal(seller)}
                                            >
                                                Authorize Categories
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr><td colSpan="6" className="text-center text-muted py-4">No sellers found.</td></tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* MODAL VIEW DESCRIPTION */}
            {showDescModal && descSeller && (
                <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} tabIndex="-1">
                    <div className="modal-dialog modal-dialog-centered modal-lg">
                        <div className="modal-content border-0 shadow">
                            <div className="modal-header bg-light">
                                <h5 className="modal-title">
                                    Description: <span className="text-primary">{descSeller.shopName}</span>
                                </h5>
                                <button type="button" className="btn-close" onClick={closeDescModal}></button>
                            </div>
                            <div className="modal-body" style={{ maxHeight: '60vh', overflowY: 'auto', whiteSpace: 'pre-wrap' }}>
                                {descSeller.description || <span className="text-muted">This seller has not provided a description yet.</span>}
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeDescModal}>Close</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAL AUTHORIZE CATEGORIES */}
            {showAuthModal && selectedSellerForAuth && (
                <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }} tabIndex="-1">
                    <div className="modal-dialog modal-dialog-centered">
                        <div className="modal-content border-0 shadow">
                            <div className="modal-header bg-light">
                                <h5 className="modal-title">
                                    Authorize Categories: <span className="text-primary">{selectedSellerForAuth.shopName}</span>
                                </h5>
                                <button type="button" className="btn-close" onClick={closeAuthorizeModal}></button>
                            </div>
                            <div className="modal-body">
                                <p className="text-muted mb-3">Select the categories this seller is allowed to use:</p>
                                <div className="d-flex flex-wrap gap-2">
                                    {availableCategories.length > 0 ? availableCategories.map(category => {
                                        // We use the category's slug or name as the key
                                        const catId = category.slug || category.name;
                                        const isSelected = selectedCategories.includes(catId);

                                        return (
                                            <span
                                                key={catId}
                                                onClick={() => toggleCategory(catId)}
                                                style={{
                                                    cursor: 'pointer',
                                                    padding: '8px 16px',
                                                    borderRadius: '20px',
                                                    backgroundColor: isSelected ? '#0d6efd' : '#f8f9fa',
                                                    color: isSelected ? 'white' : '#333',
                                                    border: `1px solid ${isSelected ? '#0d6efd' : '#dee2e6'}`,
                                                    transition: 'all 0.2s ease',
                                                    fontWeight: isSelected ? '600' : '400'
                                                }}
                                            >
                                                {category.name}
                                            </span>
                                        );
                                    }) : (
                                        <span className="text-muted">Loading categories...</span>
                                    )}
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-light" onClick={closeAuthorizeModal}>Cancel</button>
                                <button type="button" className="btn btn-primary" onClick={handleAuthorizeSeller}>
                                    Save & Authorize
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminUserDashboard;