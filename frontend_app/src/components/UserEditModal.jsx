import React, { useState, useEffect } from 'react';

// Added onAdd prop
const UserEditModal = ({ user, credentials, onUpdate, onAdd, onClose }) => {
    // Determine mode: If user object is passed, it's EDIT; otherwise, ADD.
    const isEditMode = !!user;
    const modalTitle = isEditMode ? `Edit User: ${user.fullName}` : 'Create New User';

    // Initial State Logic
    const initialFormState = {
        // User Details
        fullName: user?.fullName || '',
        address: user?.address || '',
        email: user?.email || '',

        // Credentials (Pre-filled only in EDIT mode)
        username: credentials?.username || '',
        role: credentials?.role || 'CLIENT', // Default to CLIENT for creation

        // Passwords
        password: '',          // Used for UPDATE (current password) or CREATE (new password)
        newPassword: '',       // Used for UPDATE (new password)
    };

    const [formData, setFormData] = useState(initialFormState);
    const [isSaving, setIsSaving] = useState(false);
    const [updateError, setUpdateError] = useState(null);

    // If in edit mode, and credentials were slow to load, update form state when they arrive
    useEffect(() => {
        if (isEditMode && credentials) {
            setFormData(prev => ({
                ...prev,
                username: credentials.username,
                role: credentials.role
            }));
        }
    }, [credentials, isEditMode]);


    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        setUpdateError(null);

        try {
            // Basic Validation
            if (!formData.fullName || !formData.username || !formData.role) {
                throw new Error("Full Name, Username, and Role are required.");
            }

            if (!isEditMode && !formData.password) {
                throw new Error("Password is required for user creation.");
            }

            if (isEditMode) {
                // 💡 UPDATE LOGIC
                // Pass user.id and the form data
                await onUpdate(user.id, formData);
            } else {
                // CREATE LOGIC
                // Pass only the form data (no ID needed)
                await onAdd(formData);
            }

        } catch (error) {
            setUpdateError(error.message || "An unexpected error occurred during save.");
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <header className="modal-header">
                    <h2>{modalTitle}</h2>
                    <button onClick={onClose} className="close-button">×</button>
                </header>

                <form onSubmit={handleSubmit} className="edit-form">
                    {/* User Details Fields */}
                    <div className="form-section">
                        <h4>User Details</h4>
                        <label>Full Name: <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} required /></label>
                        <label>Address: <input type="text" name="address" value={formData.address} onChange={handleChange} required /></label>
                        <label>Email: <input type="email" name="email" value={formData.email} onChange={handleChange} required /></label>
                    </div>

                    {/* Credential/Role Fields */}
                    <div className="form-section">
                        <h4>Credentials & Role</h4>
                        {/* Note: Username is editable in both modes */}
                        <label>Username: <input type="text" name="username" value={formData.username} onChange={handleChange} required /></label>

                        <label>Role:
                            <select name="role" value={formData.role} onChange={handleChange} required>
                                <option value="CLIENT">CLIENT</option>
                                <option value="ADMIN">ADMIN</option>
                            </select>
                        </label>
                    </div>

                    {/* Password Fields */}
                    <div className="form-section">
                        <h4>{isEditMode ? 'Change Password (Optional)' : 'Set Password'}</h4>

                        {isEditMode ? (
                            <>
                                {/* For update, 'password' is the current password */}
                                <label>Current Password: <input type="password" name="password" value={formData.password} onChange={handleChange} placeholder="Required to change new password" /></label>
                                {/* For update, 'newPassword' is the new one */}
                                <label>New Password: <input type="password" name="newPassword" value={formData.newPassword} onChange={handleChange} placeholder="Leave empty if not changing" /></label>
                            </>
                        ) : (
                            // For creation, we only need one 'password' field
                            <label>Password: <input type="password" name="password" value={formData.password} onChange={handleChange} required /></label>
                        )}

                    </div>

                    {updateError && <p className="error-message">{updateError}</p>}

                    <button type="submit" disabled={isSaving} className="save-button">
                        {isSaving ? 'Saving...' : (isEditMode ? 'Save Changes' : 'Create User')}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default UserEditModal;