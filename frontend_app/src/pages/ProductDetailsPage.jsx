import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productService, getProductImageUrl } from '../api/productService';
import { cartService } from '../api/cartService';
import { postService } from '../api/postService';
import { getSellerProfile, getPublicUserProfile } from '../api/userService';
import { useAuth } from '../context/AuthContext';
import BrowseNavbar from '../components/BrowseNavbar';
import '../styles/ProductDetailsPage.css';

const ProductDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { activeRole, userId } = useAuth();
    
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [seller, setSeller] = useState(null);
    const [sellerLoading, setSellerLoading] = useState(false);

    // Reviews State
    const [reviews, setReviews] = useState([]);
    
    // Add Review State (Buyer)
    const [newReviewRating, setNewReviewRating] = useState(5);
    const [newReviewBody, setNewReviewBody] = useState('');
    const [isSubmittingReview, setIsSubmittingReview] = useState(false);

    // Reply State (Seller)
    const [replyingToReviewId, setReplyingToReviewId] = useState(null);
    const [replyBody, setReplyBody] = useState('');
    const [isSubmittingReply, setIsSubmittingReply] = useState(false);

    const loadProductDetails = useCallback(async () => {
        try {
            setLoading(true);
            const response = await productService.getProductById(id);
            const productData = response.data || response;
            
            try {
                const imagesResponse = await productService.getProductImages(id);
                productData.images = imagesResponse.data || imagesResponse || [];
            } catch (imgErr) {
                console.error("Failed to load images:", imgErr);
                productData.images = [];
            }
            
            setProduct(productData);

            // Load seller profile
            if (productData.sellerId) {
                loadSellerProfile(productData.sellerId);
            }

            loadReviews();

        } catch (err) {
            setError('Failed to load product details.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, [id]);

    const loadSellerProfile = async (sellerId) => {
        try {
            setSellerLoading(true);
            try {
                const sellerData = await getSellerProfile(sellerId);
                console.log('Seller Profile Data:', sellerData);
                const profileData = sellerData.data || sellerData;

                if (profileData && profileData.shopName) {
                    setSeller({
                        storeName: profileData.shopName,
                        logo: profileData.logoUrl,
                        description: profileData.description || '',
                        avgRating: profileData.avgRating,
                        joinedDate: profileData.createdAt,
                        totalProducts: profileData.totalProducts
                    });
                    return;
                }
            } catch (sellerErr) {
                console.log("Seller profile not found, trying public profile:", sellerErr);
            }

            try {
                const publicData = await getPublicUserProfile(sellerId);
                console.log('Public Profile Data:', publicData);
                const profileData = publicData.data || publicData;
                setSeller({
                    storeName: profileData.username || profileData.email || 'Unknown Store',
                    logo: profileData.avatar,
                    description: profileData.bio || ''
                });
            } catch (publicErr) {
                console.error("Failed to load public profile:", publicErr);
                setSeller(null);
            }
        } catch (err) {
            console.error("Failed to load seller profile:", err);
            setSeller(null);
        } finally {
            setSellerLoading(false);
        }
    };

    const loadReviews = async () => {
        try {
            const reviewsResponse = await postService.getReviewsByProductId(id);
            setReviews(reviewsResponse.data || reviewsResponse || []);
        } catch (revErr) {
            console.error("Failed to load reviews:", revErr);
        }
    };

    useEffect(() => {
        loadProductDetails();
    }, [loadProductDetails]);

    const handleAddToCart = async () => {
        if (!product) return;
        try {
            await cartService.addItemToCart(product.id, product.title, product.price, quantity, product.sellerId);
            alert(`${product.title} added to cart!`);
        } catch (err) {
            console.error("Failed to add to cart", err);
            alert("Could not add to cart. Please try again.");
        }
    };

    const handleSubmitReview = async (e) => {
        e.preventDefault();
        if (!newReviewBody.trim()) {
            alert("Review text cannot be empty");
            return;
        }
        try {
            setIsSubmittingReview(true);
            await postService.createReview(userId, product.id, "", newReviewRating, newReviewBody);
            setNewReviewBody('');
            setNewReviewRating(5);
            alert("Review submitted successfully! The average rating will update shortly.");
            
            setTimeout(() => {
                loadProductDetails();
            }, 1000); 

        } catch (error) {
            console.error("Failed to submit review:", error);
            alert("Could not submit review. Please try again.");
        } finally {
            setIsSubmittingReview(false);
        }
    };

    const handleSubmitReply = async (reviewId) => {
        if (!replyBody.trim()) {
            alert("Reply text cannot be empty");
            return;
        }
        try {
            setIsSubmittingReply(true);
            await postService.addVendorReply(reviewId, userId, replyBody);
            setReplyBody('');
            setReplyingToReviewId(null);
            alert("Reply submitted successfully!");
            loadReviews();
        } catch (error) {
            console.error("Failed to submit reply:", error);
            alert("Could not submit reply. Please try again.");
        } finally {
            setIsSubmittingReply(false);
        }
    };

    if (loading) return <div className="product-details-loading">Loading product...</div>;
    if (error) return <div className="product-details-error">{error}</div>;
    if (!product) return <div className="product-details-error">Product not found.</div>;

    const isProductOwner = activeRole === 'SELLER' && product.sellerId === userId;

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
                            <span className="product-rating">★ {product.avgRating ?? product.rating ?? '0.0'} ({product.reviewCount || 0} reviews)</span>
                        </div>
                        
                        <div className="product-description-box">
                            <h3>Description</h3>
                            <p>{product.description || 'No description provided by the seller.'}</p>
                        </div>
                        
                        {/* SELLER INFORMATION SECTION */}
                        {!sellerLoading && seller && (
                            <div className="seller-info-box">
                                <h3>Seller Information</h3>
                                <div className="seller-card">
                                    {seller.logo && (
                                        <img src={seller.logo} alt={seller.storeName} className="seller-logo" />
                                    )}
                                    <div className="seller-details">
                                        <h4 className="seller-name">{seller.storeName || 'Unknown Store'}</h4>
                                        {seller.avgRating !== undefined && seller.avgRating !== null && (
                                            <div className="seller-rating">
                                                ★ {seller.avgRating.toFixed(1)} rating
                                            </div>
                                        )}
                                        {seller.description && (
                                            <p className="seller-description">{seller.description}</p>
                                        )}
                                        <div className="seller-stats">
                                            {seller.joinedDate && (
                                                <span className="stat">📅 Joined {new Date(seller.joinedDate).toLocaleDateString()}</span>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}
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

                {/* REVIEWS SECTION */}
                <div className="reviews-section mt-5">
                    <h3 className="border-bottom pb-2 mb-4">Customer Reviews</h3>

                    {activeRole === 'BUYER' && (
                        <div className="add-review-card bg-light p-4 rounded mb-4 border">
                            <h5 className="mb-3">Write a Review</h5>
                            <form onSubmit={handleSubmitReview}>
                                <div className="mb-3">
                                    <label className="form-label">Rating:</label>
                                    <select 
                                        className="form-select w-auto" 
                                        value={newReviewRating} 
                                        onChange={(e) => setNewReviewRating(Number(e.target.value))}
                                    >
                                        <option value={5}>⭐⭐⭐⭐⭐ (5/5)</option>
                                        <option value={4}>⭐⭐⭐⭐ (4/5)</option>
                                        <option value={3}>⭐⭐⭐ (3/5)</option>
                                        <option value={2}>⭐⭐ (2/5)</option>
                                        <option value={1}>⭐ (1/5)</option>
                                    </select>
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Your feedback:</label>
                                    <textarea 
                                        className="form-control" 
                                        rows="3" 
                                        placeholder="What did you like or dislike about this product?"
                                        value={newReviewBody}
                                        onChange={(e) => setNewReviewBody(e.target.value)}
                                        required
                                    ></textarea>
                                </div>
                                <button 
                                    type="submit" 
                                    className="btn btn-primary"
                                    disabled={isSubmittingReview || !newReviewBody.trim()}
                                >
                                    {isSubmittingReview ? 'Submitting...' : 'Submit Review'}
                                </button>
                            </form>
                        </div>
                    )}

                    {reviews.length === 0 ? (
                        <p className="text-muted">No reviews yet. Be the first to review this product!</p>
                    ) : (
                        <div className="reviews-list">
                            {reviews.map((review) => (
                                <div key={review.id} className="review-card card mb-3 border-0 shadow-sm">
                                    <div className="card-body">
                                        <div className="d-flex justify-content-between align-items-center mb-2">
                                            <div className="review-rating text-warning fw-bold">
                                                {'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}
                                            </div>
                                            <small className="text-muted">
                                                {new Date(review.createdAt || Date.now()).toLocaleDateString()}
                                            </small>
                                        </div>
                                        <p className="card-text">{review.body}</p>
                                        
                                        {review.reply ? (
                                            <div className="vendor-reply bg-light p-3 mt-3 border-start border-primary border-4 rounded">
                                                <strong className="text-primary d-block mb-1">Seller's Reply:</strong>
                                                <p className="mb-0 text-secondary">{review.reply.body}</p>
                                            </div>
                                        ) : (
                                            isProductOwner && (
                                                <div className="mt-3">
                                                    {replyingToReviewId === review.id ? (
                                                        <div className="reply-form p-3 border rounded bg-light">
                                                            <textarea 
                                                                className="form-control mb-2" 
                                                                rows="2" 
                                                                placeholder="Write your response to the customer..."
                                                                value={replyBody}
                                                                onChange={(e) => setReplyBody(e.target.value)}
                                                            ></textarea>
                                                            <div className="d-flex gap-2">
                                                                <button 
                                                                    className="btn btn-sm btn-success" 
                                                                    onClick={() => handleSubmitReply(review.id)}
                                                                    disabled={isSubmittingReply || !replyBody.trim()}
                                                                >
                                                                    Post Reply
                                                                </button>
                                                                <button 
                                                                    className="btn btn-sm btn-outline-secondary" 
                                                                    onClick={() => {
                                                                        setReplyingToReviewId(null);
                                                                        setReplyBody('');
                                                                    }}
                                                                >
                                                                    Cancel
                                                                </button>
                                                            </div>
                                                        </div>
                                                    ) : (
                                                        <button 
                                                            className="btn btn-sm btn-outline-primary"
                                                            onClick={() => setReplyingToReviewId(review.id)}
                                                        >
                                                            Reply to Review
                                                        </button>
                                                    )}
                                                </div>
                                            )
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProductDetailsPage;
