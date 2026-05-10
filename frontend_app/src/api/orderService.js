import { fetchWrapper } from '../utils/fetchWrapper';

const ORDERS_BASE = '/api/orders';
const ADMIN_ORDERS_BASE = '/api/admin/orders';
const SELLER_ORDERS_BASE = '/api/seller/orders';

export const orderService = {
  checkout: (deliveryAddress) =>
    fetchWrapper(`${ORDERS_BASE}/checkout`, {
      method: 'POST',
      body: JSON.stringify({ deliveryAddress }),
    }),

  getOrderHistory: () => fetchWrapper(ORDERS_BASE),

  getOrderById: (orderId) => fetchWrapper(`${ORDERS_BASE}/${orderId}`),

  cancelOrder: (orderId) =>
    fetchWrapper(`${ORDERS_BASE}/${orderId}/cancel`, {
      method: 'POST',
    }),

  // ADMIN ONLY
  getAllOrdersAdmin: () => fetchWrapper(ADMIN_ORDERS_BASE),

  updateOrderStatusAdmin: (orderId, status) =>
    fetchWrapper(`${ADMIN_ORDERS_BASE}/${orderId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    }),

  // SELLER ONLY
  getOrdersForSeller: () => fetchWrapper(SELLER_ORDERS_BASE),
  
  // A seller can also update an order's status using the regular endpoint, 
  // since we added 'SELLER' role to the @PreAuthorize of updateOrderStatus in backend
  updateOrderStatusSeller: (orderId, status) =>
    fetchWrapper(`${ORDERS_BASE}/${orderId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    }),
};
