import { fetchWrapper } from '../utils/fetchWrapper';

const CATEGORY_BASE = '/api/products/v1/categories';

export const categoryService = {
  getCategories: () => fetchWrapper(CATEGORY_BASE),

  getRootCategories: () => fetchWrapper(`${CATEGORY_BASE}/roots`),

  getCategoryById: (id) => fetchWrapper(`${CATEGORY_BASE}/${id}`),

  getChildCategories: (id) => fetchWrapper(`${CATEGORY_BASE}/${id}/children`),

  createCategory: (data) =>
    fetchWrapper(CATEGORY_BASE, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateCategory: (id, data) =>
    fetchWrapper(`${CATEGORY_BASE}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  deleteCategory: (id) =>
    fetchWrapper(`${CATEGORY_BASE}/${id}`, {
      method: 'DELETE',
    }),
};