import React, { useEffect, useMemo, useState } from 'react';
import BrowseNavbar from '../components/BrowseNavbar.jsx';
import { productService, getProductImageUrl } from '../api/productService';
import { categoryService } from '../api/categoryService';
import '../styles/BrowsePage.css';

const extractList = (response) => {
  if (Array.isArray(response)) return response;
  if (Array.isArray(response?.data)) return response.data;
  if (Array.isArray(response?.content)) return response.content;
  if (Array.isArray(response?.data?.content)) return response.data.content;
  return [];
};

const extractPageInfo = (response) => {
  const pageData = response?.data || response;

  return {
    page: pageData?.page ?? pageData?.number ?? 0,
    totalPages: pageData?.totalPages ?? 1,
    totalElements: pageData?.totalElements ?? extractList(response).length,
  };
};

const BrowsePage = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);

  const [loading, setLoading] = useState(false);
  const [loadingCategories, setLoadingCategories] = useState(false);

  const [pageInfo, setPageInfo] = useState({
    page: 0,
    totalPages: 1,
    totalElements: 0,
  });

  const [filters, setFilters] = useState({
    search: '',
    categoryId: '',
    minPrice: '',
    maxPrice: '',
    minRating: '',
    sortBy: 'createdAt',
    sortDir: 'desc',
  });

  const queryParams = useMemo(() => {
    const params = {
      page: pageInfo.page,
      size: 12,
      sortBy: filters.sortBy,
      sortDir: filters.sortDir,
    };

    if (filters.search.trim()) {
      params.search = filters.search.trim();
    }

    if (filters.categoryId) {
      params.categoryId = filters.categoryId;
    }

    if (filters.minPrice !== '') {
      params.minPrice = filters.minPrice;
    }

    if (filters.maxPrice !== '') {
      params.maxPrice = filters.maxPrice;
    }

    if (filters.minRating !== '') {
      params.minRating = filters.minRating;
    }

    return params;
  }, [filters, pageInfo.page]);

  const loadCategories = async () => {
    try {
      setLoadingCategories(true);
      const response = await categoryService.getCategories();
      setCategories(extractList(response));
    } catch (error) {
      console.error('Load categories error:', error);
    } finally {
      setLoadingCategories(false);
    }
  };

  const loadProducts = async () => {
    try {
      setLoading(true);

      const response = await productService.getProducts(queryParams);
      const productList = extractList(response);

      const productsWithImages = await Promise.all(
        productList.map(async (product) => {
          try {
            const imagesResponse = await productService.getProductImages(product.id);
            return {
              ...product,
              images: extractList(imagesResponse),
            };
          } catch (error) {
            console.error(`Images error for product ${product.id}:`, error);
            return {
              ...product,
              images: product.images || [],
            };
          }
        })
      );

      setProducts(productsWithImages);
      setPageInfo((prev) => ({
        ...prev,
        ...extractPageInfo(response),
      }));
    } catch (error) {
      console.error('Load products error:', error);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    loadProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [queryParams]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;

    setFilters((prev) => ({
      ...prev,
      [name]: value,
    }));

    setPageInfo((prev) => ({
      ...prev,
      page: 0,
    }));
  };

  const handleResetFilters = () => {
    setFilters({
      search: '',
      categoryId: '',
      minPrice: '',
      maxPrice: '',
      minRating: '',
      sortBy: 'createdAt',
      sortDir: 'desc',
    });

    setPageInfo({
      page: 0,
      totalPages: 1,
      totalElements: 0,
    });
  };

  const goToPreviousPage = () => {
    setPageInfo((prev) => ({
      ...prev,
      page: Math.max(prev.page - 1, 0),
    }));
  };

  const goToNextPage = () => {
    setPageInfo((prev) => ({
      ...prev,
      page: Math.min(prev.page + 1, Math.max(prev.totalPages - 1, 0)),
    }));
  };

  return (
    <div className="browse-page-wrapper">
      <div className="browse-page-container">
        <BrowseNavbar pageTitle="Browse Products" />

        <div className="browse-content-card">
          <div className="browse-header-row">
            <div>
              <h2 className="browse-section-title">Product Catalog</h2>
              <p className="browse-section-subtitle">
                Search and filter available products by category, price, and rating.
              </p>
            </div>

            <span className="browse-products-count">
              {pageInfo.totalElements} products found
            </span>
          </div>

          <div className="browse-filters-card">
            <div className="browse-filter-field browse-filter-search">
              <label>Search product</label>
              <input
                name="search"
                value={filters.search}
                onChange={handleFilterChange}
                placeholder="e.g. iPhone, laptop, headphones..."
              />
            </div>

            <div className="browse-filter-field">
              <label>Category</label>
              <select
                name="categoryId"
                value={filters.categoryId}
                onChange={handleFilterChange}
                disabled={loadingCategories}
              >
                <option value="">All categories</option>

                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name || category.title}
                  </option>
                ))}
              </select>
            </div>

            <div className="browse-filter-field">
              <label>Minimum price</label>
              <input
                name="minPrice"
                type="number"
                min="0"
                step="0.01"
                value={filters.minPrice}
                onChange={handleFilterChange}
                placeholder="0"
              />
            </div>

            <div className="browse-filter-field">
              <label>Maximum price</label>
              <input
                name="maxPrice"
                type="number"
                min="0"
                step="0.01"
                value={filters.maxPrice}
                onChange={handleFilterChange}
                placeholder="5000"
              />
            </div>

            <div className="browse-filter-field">
              <label>Minimum rating</label>
              <select
                name="minRating"
                value={filters.minRating}
                onChange={handleFilterChange}
              >
                <option value="">Any rating</option>
                <option value="1">1+ stars</option>
                <option value="2">2+ stars</option>
                <option value="3">3+ stars</option>
                <option value="4">4+ stars</option>
                <option value="4.5">4.5+ stars</option>
              </select>
            </div>

            <div className="browse-filter-field">
              <label>Sort by</label>
              <select
                name="sortBy"
                value={filters.sortBy}
                onChange={handleFilterChange}
              >
                <option value="createdAt">Newest</option>
                <option value="price">Price</option>
                <option value="averageRating">Rating</option>
                <option value="title">Name</option>
              </select>
            </div>

            <div className="browse-filter-field">
              <label>Order</label>
              <select
                name="sortDir"
                value={filters.sortDir}
                onChange={handleFilterChange}
              >
                <option value="desc">Descending</option>
                <option value="asc">Ascending</option>
              </select>
            </div>

            <button
              type="button"
              className="browse-reset-btn"
              onClick={handleResetFilters}
            >
              Reset filters
            </button>
          </div>

          {loading && (
            <p className="browse-empty-text">Loading products...</p>
          )}

          {!loading && products.length === 0 && (
            <p className="browse-empty-text">
              No products match the selected filters.
            </p>
          )}

          <div className="browse-products-grid">
            {products.map((product) => (
              <article className="browse-product-card" key={product.id}>
                <div className="browse-product-image">
                  {product.images?.length > 0 && product.images[0]?.url ? (
                    <img
                      src={getProductImageUrl(product.images[0].url)}
                      alt={product.title}
                    />
                  ) : (
                    <span>No image</span>
                  )}
                </div>

                <div className="browse-product-body">
                  <div className="browse-product-title-row">
                    <h3>{product.title}</h3>
                    <span className="browse-product-price">
                      {product.price} {product.currency || 'RON'}
                    </span>
                  </div>

                  <p className="browse-product-description">
                    {product.description || 'No description available'}
                  </p>

                  <div className="browse-product-meta">
                    <span>Stock: {product.stock}</span>
                    <span>
                      Rating:{' '}
                      {product.averageRating ?? product.rating ?? 'N/A'}
                    </span>
                  </div>

                  <button type="button" className="browse-details-btn">
                    Order now
                  </button>
                </div>
              </article>
            ))}
          </div>

          {pageInfo.totalPages > 1 && (
            <div className="browse-pagination">
              <button
                type="button"
                onClick={goToPreviousPage}
                disabled={pageInfo.page <= 0}
              >
                Previous
              </button>

              <span>
                Page {pageInfo.page + 1} of {pageInfo.totalPages}
              </span>

              <button
                type="button"
                onClick={goToNextPage}
                disabled={pageInfo.page >= pageInfo.totalPages - 1}
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default BrowsePage;