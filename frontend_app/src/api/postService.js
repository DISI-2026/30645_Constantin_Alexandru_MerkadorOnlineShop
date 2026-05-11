import { fetchWrapper } from '../utils/fetchWrapper';

const REVIEWS_BASE = '/api/reviews';

export const postService = {
  // Get all reviews for a specific product (Public)
  getReviewsByProductId: (productId) => 
    fetchWrapper(`${REVIEWS_BASE}/product/${productId}`),

  // Create a new review (Requires BUYER role)
  createReview: (customerId, productId, orderId, rating, body, sellerId) =>
    fetchWrapper(REVIEWS_BASE, {
      method: 'POST',
      body: JSON.stringify({
        customerId,
        productId,
        orderId, 
        rating,
        body,
        sellerId
      }),
    }),

  // Add a vendor reply to an existing review (Requires SELLER role)
  addVendorReply: (reviewId, vendorId, body) =>
    fetchWrapper(`${REVIEWS_BASE}/${reviewId}/reply`, {
      method: 'POST',
      body: JSON.stringify({
        vendorId,
        body
      }),
    }),
};
