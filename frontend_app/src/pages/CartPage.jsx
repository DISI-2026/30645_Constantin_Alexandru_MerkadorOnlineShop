import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../api/cartService';
import { orderService } from '../api/orderService';
import BrowseNavbar from '../components/BrowseNavbar';
import '../styles/CartPage.css';

const CartPage = () => {
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [deliveryAddress, setDeliveryAddress] = useState('');
    const navigate = useNavigate();

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

    useEffect(() => {
        loadCart();
    }, [loadCart]);

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

    const handleCheckout = async () => {
        if (!deliveryAddress.trim()) {
            setError('Please enter a delivery address.');
            return;
        }
        try {
            await orderService.checkout(deliveryAddress);
            alert('Order placed successfully!');
            navigate('/buyer'); // Redirect to buyer dashboard or order history
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
                                        />
                                        <button onClick={() => handleRemoveItem(item.productId)}>Remove</button>
                                    </div>
                                </li>
                            ))}
                        </ul>
                        <div className="cart-summary">
                            <h3>Total: ${cart.total.toFixed(2)}</h3>
                            <div className="checkout-section">
                                <input
                                    type="text"
                                    value={deliveryAddress}
                                    onChange={(e) => setDeliveryAddress(e.target.value)}
                                    placeholder="Enter delivery address"
                                />
                                <button onClick={handleCheckout} disabled={!deliveryAddress.trim()}>
                                    Place Order
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    <p>Your cart is empty.</p>
                )}
            </div>
        </div>
    );
};

export default CartPage;
