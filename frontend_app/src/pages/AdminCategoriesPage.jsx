import React, { useEffect, useState } from 'react';
import MainNavbar from '../components/MainNavbar.jsx';
import { categoryService } from '../api/categoryService';
import '../styles/AdminCategoriesPage.css';

const extractList = (response) => {
  if (Array.isArray(response)) return response;
  if (Array.isArray(response?.data)) return response.data;
  if (Array.isArray(response?.content)) return response.content;
  if (Array.isArray(response?.data?.content)) return response.data.content;
  return [];
};

const AdminCategoriesPage = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);

  const [form, setForm] = useState({
    name: '',
    slug: '',
    description: '',
  });

  const getErrorMessage = (error, fallback) => {
    return (
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error?.response?.data?.details ||
      error?.message ||
      fallback
    );
  };

  const generateSlugFromName = (name) => {
    return name
      .toLowerCase()
      .trim()
      .replace(/[ăâ]/g, 'a')
      .replace(/[î]/g, 'i')
      .replace(/[șş]/g, 's')
      .replace(/[țţ]/g, 't')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  };

  const loadCategories = async () => {
    try {
      setLoading(true);

      const response = await categoryService.getCategories();
      setCategories(extractList(response));
    } catch (error) {
      console.error('Load categories error:', error);
      alert(getErrorMessage(error, 'Could not load categories.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  const handleChange = (e) => {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const handleGenerateSlug = () => {
    setForm((prev) => ({
      ...prev,
      slug: generateSlugFromName(prev.name),
    }));
  };

  const handleCreateCategory = async (e) => {
    e.preventDefault();

    if (!form.name.trim()) {
      alert('Please enter the category name.');
      return;
    }

    try {
      setCreating(true);

      const payload = {
        name: form.name.trim(),
        slug: form.slug.trim() || generateSlugFromName(form.name),
        description: form.description.trim(),
      };

      await categoryService.createCategory(payload);

      setForm({
        name: '',
        slug: '',
        description: '',
      });

      await loadCategories();

      alert('Category created successfully.');
    } catch (error) {
      console.error('Create category error:', error);
      alert(getErrorMessage(error, 'Could not create category.'));
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteCategory = async (categoryId) => {
    const confirmed = window.confirm(
      'Are you sure you want to delete this category? If there are products in this category, the backend may reject the deletion.'
    );

    if (!confirmed) return;

    try {
      await categoryService.deleteCategory(categoryId);
      await loadCategories();
      alert('Category deleted.');
    } catch (error) {
      console.error('Delete category error:', error);
      alert(getErrorMessage(error, 'Could not delete category.'));
    }
  };

  return (
    <div className="admin-categories-page">
      <div className="admin-categories-container">
        <MainNavbar pageTitle="Category Management" />

        <section className="admin-categories-hero">
          <div>
            <span className="admin-categories-badge">Admin</span>
            <h1>Product Categories</h1>
            <p>
              Create and manage the categories that sellers will use when adding products.
            </p>
          </div>

          <button type="button" onClick={loadCategories}>
            Refresh
          </button>
        </section>

        <section className="admin-categories-layout">
          <form className="admin-category-form" onSubmit={handleCreateCategory}>
            <h2>Add Category</h2>

            <label>
              Category Name
              <input
                name="name"
                placeholder="e.g. Laptops"
                value={form.name}
                onChange={handleChange}
                required
              />
            </label>

            <label>
              Slug
              <div className="admin-category-slug-row">
                <input
                  name="slug"
                  placeholder="e.g. laptops"
                  value={form.slug}
                  onChange={handleChange}
                  required
                />

                <button type="button" onClick={handleGenerateSlug}>
                  Generate
                </button>
              </div>
            </label>

            <label>
              Description
              <textarea
                name="description"
                placeholder="Category description"
                value={form.description}
                onChange={handleChange}
              />
            </label>

            <button
              className="admin-category-submit"
              type="submit"
              disabled={creating}
            >
              {creating ? 'Creating...' : 'Create Category'}
            </button>
          </form>

          <section className="admin-category-list">
            <div className="admin-category-list-header">
              <div>
                <h2>Existing Categories</h2>
                <p>{categories.length} categories</p>
              </div>
            </div>

            {loading && <p className="admin-category-empty">Loading...</p>}

            {!loading && categories.length === 0 && (
              <p className="admin-category-empty">No categories available.</p>
            )}

            <div className="admin-category-grid">
              {categories.map((category) => (
                <article className="admin-category-card" key={category.id}>
                  <div>
                    <h3>{category.name || category.title}</h3>
                    <p>{category.description || 'No description'}</p>
                    <span>{category.slug}</span>
                  </div>

                  <button
                    type="button"
                    onClick={() => handleDeleteCategory(category.id)}
                  >
                    Delete
                  </button>
                </article>
              ))}
            </div>
          </section>
        </section>
      </div>
    </div>
  );
};

export default AdminCategoriesPage;