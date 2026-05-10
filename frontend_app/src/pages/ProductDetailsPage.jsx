import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productService, getProductImageUrl } from '../api/productService';
import { cartService } from '../api/cartService';
import { useAuth } from '../context/AuthContext';
import BrowseNavbar from '../components/BrowseNavbar';
import '../styles/ProductDetailsPage.css';

const ProductDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { activeRole } = useAuth();
    
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [quantity, setQuantity] = useState(1);

    useEffect(() => {
        const loadProductDetails = async () => {
            try {
                setLoading(true);
                const response = await productService.getProductById(id);
                const productData = response.data || response;
                
                // Fetch images for the product
                try {
                    const imagesResponse = await productService.getProductImages(id);
                    productData.images = imagesResponse.data || imagesResponse || [];
                } catch (imgErr) {
                    console.error("Failed to load images:", imgErr);
                    productData.images = [];
                }
                
                setProduct(productData);
            } catch (err) {
                setError('Failed to load product details.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadProductDetails();
    }, [id]);

    const handleAddToCart = async () => {
        if (!product) return;
        try {
            await cartService.addItemToCart(product.id, product.title, product.price, quantity);
            alert(`${product.title} added to cart!`);
        } catch (err) {
            console.error("Failed to add to cart", err);
            alert("Could not add to cart. Please try again.");
        }
    };

    if (loading) return <div className="product-details-loading">Loading product...</div>;
    if (error) return <div className="product-details-error">{error}</div>;
    if (!product) return <div className="product-details-error">Product not found.</div>;

    return (
        <div className="product-details-wrapper">
            <BrowseNavbar pageTitle="Product Details" />
            <div className="product-details-container">
                <button className="back-button" onClick={() => navigate(-1)}>
                    &larr; Back
                </button>
                
                <div className="product-details-content">
                    <div className="product-details-image-section">
                        {product.images && product.images.length > 0 ? (
                            <img
                                src={getProductImageUrl(product.images[0].url)}
                                alt={product.title}
                                className="product-main-image"
                            />
                        ) : (
                            <div className="product-no-image">No Image Available</div>
                        )}
                        
                        {product.images && product.images.length > 1 && (
                            <div className="product-thumbnails">
                                {product.images.slice(1).map(img => (
                                    <img 
                                        key={img.id} 
                                        src={getProductImageUrl(img.url)} 
                                        alt="thumbnail" 
                                        className="product-thumbnail" 
                                    />
                                ))}
                            </div>
                        )}
                    </div>
                    
                    <div className="product-details-info-section">
                        <h1 className="product-title">{product.title}</h1>
                        <p className="product-price">{product.price} {product.currency || 'RON'}</p>
                        
                        <div className="product-meta">
                            <span className="product-stock">Stock: {product.stock} available</span>
                            <span className="product-rating">Rating: {product.averageRating ?? product.rating ?? 'N/A'}</span>
                        </div>
                        
                        <div className="product-description-box">
                            <h3>Description</h3>
                            <p>{product.description || 'No description provided by the seller.'}</p>
                        </div>
                        
                        {activeRole === 'BUYER' && (
                            <div className="product-add-to-cart-section">
                                <div className="quantity-selector">
                                    <label htmlFor="quantity">Quantity:</label>
                                    <input 
                                        type="number" 
                                        id="quantity" 
                                        value={quantity} 
                                        onChange={(e) => setQuantity(Math.max(1, Math.min(product.stock, parseInt(e.target.value) || 1)))} 
                                        min="1" 
                                        max={product.stock}
                                    />
                                </div>
                                <button 
                                    className="add-to-cart-button" 
                                    onClick={handleAddToCart}
                                    disabled={product.stock <= 0}
                                >
                                    {product.stock > 0 ? 'Add to Cart' : 'Out of Stock'}
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductDetailsPage;
