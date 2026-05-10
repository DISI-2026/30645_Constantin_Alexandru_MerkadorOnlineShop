import { fetchWrapper } from '../utils/fetchWrapper';

const ORDERS_BASE = '/api/orders';

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
};
