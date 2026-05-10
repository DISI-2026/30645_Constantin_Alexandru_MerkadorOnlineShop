import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../api/cartService';
import { orderService } from '../api/orderService';
import { getUserAddresses } from '../api/userService';
import { useAuth } from '../context/AuthContext';
import BrowseNavbar from '../components/BrowseNavbar';
import '../styles/CartPage.css';

const CartPage = () => {
    const { userId } = useAuth();
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const [savedAddresses, setSavedAddresses] = useState([]);
    const [selectedAddressId, setSelectedAddressId] = useState('custom');
    const [customAddress, setCustomAddress] = useState({
        addressLine: '',
        city: '',
        postalCode: '',
        country: ''
    });

    const loadCart = useCallback(async () => {
        try {
            setLoading(true);
            const cartData = await cartService.getCart();
            setCart(cartData.data || cartData);
        } catch (err) {
            setError('Failed to load cart. Please try again.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, []);

    const loadAddresses = useCallback(async () => {
        if (!userId) return;
        try {
            const addressesResponse = await getUserAddresses(userId);
            const addresses = addressesResponse.data || addressesResponse || [];
            setSavedAddresses(addresses);

            if (addresses.length > 0) {
                // Try to find default address
                const defaultAddr = addresses.find(a => a.isDefault);
                if (defaultAddr) {
                    setSelectedAddressId(defaultAddr.id);
                } else {
                    setSelectedAddressId(addresses[0].id);
                }
            }
        } catch (err) {
            console.log('Failed to load addresses.');
        }
    }, [userId]);

    useEffect(() => {
        loadCart();
        loadAddresses();
    }, [loadCart, loadAddresses]);

    const handleQuantityChange = async (productId, quantity) => {
        if (quantity < 1) return;
        try {
            const updatedCart = await cartService.updateItemQuantity(productId, quantity);
            setCart(updatedCart.data || updatedCart);
        } catch (err) {
            setError('Failed to update quantity.');
        }
    };

    const handleRemoveItem = async (productId) => {
        try {
            const updatedCart = await cartService.removeItemFromCart(productId);
            setCart(updatedCart.data || updatedCart);
        } catch (err) {
            setError('Failed to remove item.');
        }
    };

    const handleClearCart = async () => {
        if (!window.confirm("Are you sure you want to empty your cart?")) return;
        try {
            await cartService.clearCart();
            setCart({ items: [], total: 0 });
        } catch (err) {
            setError('Failed to clear cart.');
        }
    };

    const handleCustomAddressChange = (e) => {
        const { name, value } = e.target;
        setCustomAddress(prev => ({ ...prev, [name]: value }));
    };

    const handleCheckout = async () => {
        let finalAddress = '';

        if (selectedAddressId === 'custom') {
            if (!customAddress.addressLine || !customAddress.city || !customAddress.postalCode || !customAddress.country) {
                setError('Please fill in all address fields.');
                return;
            }
            finalAddress = `${customAddress.addressLine}, ${customAddress.city}, ${customAddress.postalCode}, ${customAddress.country}`;
        } else {
            const addr = savedAddresses.find(a => a.id === selectedAddressId);
            if (!addr) {
                setError('Invalid address selected.');
                return;
            }
            finalAddress = `${addr.addressLine}, ${addr.city}, ${addr.postalCode}, ${addr.country}`;
        }

        try {
            await orderService.checkout(finalAddress);
            alert('Order placed successfully!');
            navigate('/buyer');
        } catch (err) {
            setError('Checkout failed. Please try again.');
            console.error(err);
        }
    };

    if (loading) {
        return <div>Loading cart...</div>;
    }

    return (
        <div className="cart-page-container">
            <BrowseNavbar pageTitle="Your Shopping Cart" />
            <div className="cart-content">
                {error && <p className="cart-error">{error}</p>}
                {cart && cart.items && cart.items.length > 0 ? (
                    <div className="cart-details">
                        <div className="cart-header-actions">
                            <button 
                                onClick={handleClearCart} 
                                className="btn btn-outline-danger btn-sm"
                            >
                                Empty Cart
                            </button>
                        </div>
                        
                        <ul className="cart-items-list">
                            {cart.items.map((item) => (
                                <li key={item.productId} className="cart-item">
                                    <div className="item-info">
                                        <h4>{item.productTitle}</h4>
                                        <p>Price: ${item.unitPrice.toFixed(2)}</p>
                                    </div>
                                    <div className="item-actions">
                                        <input
                                            type="number"
                                            value={item.quantity}
                                            onChange={(e) => handleQuantityChange(item.productId, parseInt(e.target.value, 10))}
                                            min="1"
                                            className="form-control d-inline-block"
                                        />
                                        <button className="btn btn-danger btn-sm" onClick={() => handleRemoveItem(item.productId)}>Remove</button>
                                    </div>
                                </li>
                            ))}
                        </ul>
                        
                        <div className="cart-summary-section row mt-4">
                            <div className="col-md-7">
                                <div className="checkout-address-card p-3 border rounded bg-light">
                                    <h5 className="mb-3">Delivery Address</h5>
                                    
                                    {savedAddresses.length > 0 && (
                                        <div className="mb-3">
                                            <label className="form-label small text-muted">Select a saved address</label>
                                            <select 
                                                className="form-select"
                                                value={selectedAddressId}
                                                onChange={(e) => setSelectedAddressId(e.target.value)}
                                            >
                                                {savedAddresses.map(addr => (
                                                    <option key={addr.id} value={addr.id}>
                                                        {addr.label} ({addr.addressLine}, {addr.city}) {addr.isDefault ? ' - Default' : ''}
                                                    </option>
                                                ))}
                                                <option value="custom">+ Enter a new address</option>
                                            </select>
                                        </div>
                                    )}

                                    {selectedAddressId === 'custom' && (
                                        <div className="custom-address-form row g-2">
                                            <div className="col-12">
                                                <input type="text" className="form-control form-control-sm" name="addressLine" placeholder="Street Address" value={customAddress.addressLine} onChange={handleCustomAddressChange} />
                                            </div>
                                            <div className="col-md-6">
                                                <input type="text" className="form-control form-control-sm" name="city" placeholder="City" value={customAddress.city} onChange={handleCustomAddressChange} />
                                            </div>
                                            <div className="col-md-6">
                                                <input type="text" className="form-control form-control-sm" name="postalCode" placeholder="Postal Code" value={customAddress.postalCode} onChange={handleCustomAddressChange} />
                                            </div>
                                            <div className="col-12">
                                                <input type="text" className="form-control form-control-sm" name="country" placeholder="Country" value={customAddress.country} onChange={handleCustomAddressChange} />
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                            
                            <div className="col-md-5 d-flex flex-column justify-content-center align-items-end">
                                <h2 className="mb-3">Total: ${cart.total.toFixed(2)}</h2>
                                <button className="btn btn-success btn-lg px-5" onClick={handleCheckout}>
                                    Place Order
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    <p style={{ textAlign: 'center', fontSize: '1.2rem', marginTop: '2rem' }}>Your cart is empty.</p>
                )}
            </div>
        </div>
    );
};

export default CartPage;
