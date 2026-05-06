import { fetchWrapper } from '../utils/fetchWrapper';

const PRODUCT_BASE = '/api/products/v1/products';

export function getProductImageUrl(imageUrl) {
  if (!imageUrl) return '';

  // URL absolut deja valid
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }

  // Deja este URL prin Traefik
  if (imageUrl.startsWith('/api/uploads/products')) {
    return imageUrl;
  }

  // Backend-ul salvează/trimite /uploads/products/...
  // Browserul trebuie să ceară /api/uploads/products/...
  if (imageUrl.startsWith('/uploads/products')) {
    return `/api${imageUrl}`;
  }

  // Dacă vine doar cu slash, ex: /poza.jpg
  if (imageUrl.startsWith('/')) {
    return `/api/uploads/products${imageUrl}`;
  }

  // Dacă vine doar numele fișierului, ex: poza.jpg
  return `/api/uploads/products/${imageUrl}`;
}

export const productService = {
  getProducts: (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return fetchWrapper(query ? `${PRODUCT_BASE}?${query}` : PRODUCT_BASE);
  },

  getMyProducts: (params = {}) => {
    const query = new URLSearchParams(params).toString();
    return fetchWrapper(query ? `${PRODUCT_BASE}/my?${query}` : `${PRODUCT_BASE}/my`);
  },

  getProductById: (id) => fetchWrapper(`${PRODUCT_BASE}/${id}`),

  createProduct: (data) =>
    fetchWrapper(PRODUCT_BASE, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateProduct: (id, data) =>
    fetchWrapper(`${PRODUCT_BASE}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  deleteProduct: (id) =>
    fetchWrapper(`${PRODUCT_BASE}/${id}`, {
      method: 'DELETE',
    }),

  updateStock: (id, stock) =>
    fetchWrapper(`${PRODUCT_BASE}/${id}/stock`, {
      method: 'PATCH',
      body: JSON.stringify({ stock }),
    }),

  updatePrice: (id, price) =>
    fetchWrapper(`${PRODUCT_BASE}/${id}/price`, {
      method: 'PATCH',
      body: JSON.stringify({ price }),
    }),

  activateProduct: (id) =>
    fetchWrapper(`${PRODUCT_BASE}/${id}/activate`, {
      method: 'PATCH',
    }),

  deactivateProduct: (id) =>
    fetchWrapper(`${PRODUCT_BASE}/${id}/deactivate`, {
      method: 'PATCH',
    }),

  // =========================
  // Product images
  // Backend:
  // /products/v1/products/{productId}/images
  // Frontend through Traefik:
  // /api/products/v1/products/{productId}/images
  // =========================

  getProductImages: (productId) =>
    fetchWrapper(`${PRODUCT_BASE}/${productId}/images`),

  addProductImage: (productId, file, altText = '', sortOrder = 0) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('altText', altText);
  formData.append('sortOrder', String(sortOrder));

  return fetchWrapper(`${PRODUCT_BASE}/${productId}/images`, {
    method: 'POST',
    body: formData,
  });
},

  deleteProductImage: (productId, imageId) =>
    fetchWrapper(`${PRODUCT_BASE}/${productId}/images/${imageId}`, {
      method: 'DELETE',
    }),

  reorderProductImages: (productId, orderedImageIds) =>
    fetchWrapper(`${PRODUCT_BASE}/${productId}/images/reorder`, {
      method: 'PUT',
      body: JSON.stringify({ orderedImageIds }),
    }),
};