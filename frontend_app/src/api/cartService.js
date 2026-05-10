import { fetchWrapper } from '../utils/fetchWrapper';

const CART_BASE = '/api/cart';

export const cartService = {
  getCart: () => fetchWrapper(CART_BASE),

  addItemToCart: (productId, productTitle, unitPrice, quantity = 1, sellerId) =>
    fetchWrapper(`${CART_BASE}/items`, {
      method: 'POST',
      body: JSON.stringify({ productId, productTitle, unitPrice, quantity, sellerId }),
    }),

  updateItemQuantity: (productId, quantity) =>
    fetchWrapper(`${CART_BASE}/items/${productId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    }),

  removeItemFromCart: (productId) =>
    fetchWrapper(`${CART_BASE}/items/${productId}`, {
      method: 'DELETE',
    }),

  clearCart: () =>
    fetchWrapper(CART_BASE, {
      method: 'DELETE',
    }),
};
