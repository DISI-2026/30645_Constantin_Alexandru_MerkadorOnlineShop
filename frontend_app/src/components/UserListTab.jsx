import React, { useState, useEffect, useCallback } from 'react';
import { getAllUsers, createUser, deleteUser, updateUser } from '../api/userService';
import { getCredentialsById } from '../api/credentialsService';
import UserEditModal from './UserEditModal'; // The modal component

const UserListTab = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Modal State
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [currentUserToEdit, setCurrentUserToEdit] = useState(null);
    const [currentUserCredentials, setCurrentUserCredentials] = useState(null);
    const [isEditLoading, setIsEditLoading] = useState(false);

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const userList = await getAllUsers();
            setUsers(userList);
        } catch (err) {
            setError(err.message || "Failed to load user data.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    const handleDelete = async (userId, fullName) => {
        if (!window.confirm(`Are you sure you want to delete user: ${fullName}?`)) return;

        try {
            await deleteUser(userId);
            await fetchUsers();
        } catch (error) {
            alert(`Deletion failed: ${error.message}`);
        }
    };

    // Handler for creation
    const handleCreate = async (formData) => {
        try{
            await createUser(formData);
            setIsModalOpen(false);
            setCurrentUserToEdit(null); // Clean up state
            setCurrentUserCredentials(null); // Clean up state
            await fetchUsers();
        }catch(error){
            // Re-throw so the modal can display the error
            throw error;
        }
    }

    // Handler for update
    const handleUpdate = async (id, formData) => {
        try {
            await updateUser(id, formData);
            setIsModalOpen(false);
            setCurrentUserToEdit(null); // Clean up state
            setCurrentUserCredentials(null); // Clean up state
            fetchUsers();
        } catch (error) {
            alert(`Update failed: ${error.message}`);
            throw error;
        }
    };

    const handleEditClick = async (user) => {
        setIsEditLoading(true);
        setError(null);
        setCurrentUserToEdit(user);

        try {
            const credentials = await getCredentialsById(user.id);
            setCurrentUserCredentials(credentials);
            setIsModalOpen(true);
        } catch (error) {
            setError("Failed to fetch user credentials for editing: " + error.message);
        } finally {
            setIsEditLoading(false);
        }
    };

    const handleOpenAddModal = () => {
        setCurrentUserToEdit(null);
        setCurrentUserCredentials(null);
        setIsModalOpen(true);
    };

    // Renders a single user card/container
    const renderUserCard = (user) => (
        <div key={user.id} className="user-card">
            <h3 className="card-name">{user.fullName}</h3>
            <p><strong>Email:</strong> {user.email}</p>
            <p><strong>Address:</strong> {user.address}</p>
            <p className="card-dates">Created: {new Date(user.createdDate).toLocaleDateString()}</p>

            <div className="card-actions">
                <button
                    onClick={() => handleEditClick(user)}
                    disabled={isEditLoading}
                    className="action-edit"
                >
                    {isEditLoading && currentUserToEdit?.id === user.id ? 'Loading...' : 'Update'}
                </button>
                <button
                    onClick={() => handleDelete(user.id, user.fullName)}
                    className="action-delete"
                >
                    Delete
                </button>
            </div>
        </div>
    );

    // --- RENDER ---
    if (loading) return <p className="loading-message">Loading user data...</p>;
    if (error) return <p className="error-message">Error: {error}</p>;

    return (
        <>
            <div className="top-controls">
                <button
                    onClick={handleOpenAddModal}
                    className="add-user-button"
                >
                    + Add New User
                </button>
            </div>

            <div className="user-list-grid">
                {users.map(renderUserCard)}
            </div>

            {/* User Edit/Add Modal */}
            {isModalOpen && (
                <UserEditModal
                    user={currentUserToEdit}
                    credentials={currentUserCredentials}
                    onUpdate={handleUpdate}
                    onAdd={handleCreate}
                    onClose={() => {
                        setIsModalOpen(false);
                        setCurrentUserToEdit(null);
                        setCurrentUserCredentials(null);
                    }}
                />
            )}
            {users.length === 0 && <p className="no-data-message">No users found in the system.</p>}
        </>
    );
};

export default UserListTab;