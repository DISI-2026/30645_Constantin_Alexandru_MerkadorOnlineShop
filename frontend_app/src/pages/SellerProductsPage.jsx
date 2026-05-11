import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import MainNavbar from '../components/MainNavbar.jsx';
import { useAuth } from '../context/AuthContext';
import { productService, getProductImageUrl } from '../api/productService';
import { categoryService } from '../api/categoryService';
import { orderService } from '../api/orderService';
import { getSellerProfile } from '../api/userService';
import '../styles/SellerProductsPage.css';

const extractList = (response) => {
  if (Array.isArray(response)) return response;
  if (Array.isArray(response?.data)) return response.data;
  if (Array.isArray(response?.content)) return response.content;
  if (Array.isArray(response?.data?.content)) return response.data.content;
  return [];
};

const getCreatedProductFromResponse = (response) => {
  if (response?.data?.id) return response.data;
  if (response?.id) return response;
  if (response?.data?.data?.id) return response.data.data;
  return null;
};

const getProductFromResponse = (response) => {
  if (response?.data?.id) return response.data;
  if (response?.id) return response;
  if (response?.data?.data?.id) return response.data.data;
  return null;
};

const SellerProductsPage = () => {
  const { userId } = useAuth();

  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('products'); // 'products' or 'orders'

  // --- Products State ---
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);

  const [isProfileMissing, setIsProfileMissing] = useState(false);

  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedProductImages, setSelectedProductImages] = useState([]);

  const [loadingProducts, setLoadingProducts] = useState(false);
  const [creating, setCreating] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);

  const [productForm, setProductForm] = useState({
    title: '',
    slug: '',
    description: '',
    price: '',
    currency: 'RON',
    stock: '',
    categoryId: '',
    images: [],
  });

  const [imageForm, setImageForm] = useState({
    file: null,
  });

  // --- Orders State ---
  const [orders, setOrders] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);

  const getErrorMessage = (error, fallback) => {
    return (
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error?.response?.data?.details ||
      error?.message ||
      fallback
    );
  };
  const loadData = useCallback(async () => {
    try {
      setLoadingProducts(true);

        // Load the seller profile if it exists
        let profileData = null;
        try {
            const profileRes = await getSellerProfile(userId);
            profileData = profileRes.data || profileRes;
        } catch (err) {
            if (err?.response?.status === 404) {
                setIsProfileMissing(true);
                setLoadingProducts(false);
                return;
            }
        }

        // Load categories
        const catRes = await categoryService.getCategories();
        const allCategories = extractList(catRes);

        // If verified, we only show categories that the user is authorized to sell in
        if (profileData?.verified && profileData?.authorizedCategories?.length > 0) {
            const authorizedSet = new Set(profileData.authorizedCategories);
            // We check for the presence of the category name, slug, and title in the authorized set
            const filteredCats = allCategories.filter(c =>
                authorizedSet.has(c.name) || authorizedSet.has(c.slug) || authorizedSet.has(c.title)
            );
            setCategories(filteredCats);

            if (filteredCats.length > 0) {
                setProductForm(prev => ({ ...prev, categoryId: prev.categoryId || filteredCats[0].id }));
            }
        } else {
            // If not verified, we show all categories
            setCategories(allCategories);
            if (allCategories.length > 0) {
                setProductForm(prev => ({ ...prev, categoryId: prev.categoryId || allCategories[0].id }));
            }
        }

        // Load products
        const response = await productService.getMyProducts({ page: 0, size: 50 });
        const productList = extractList(response);
        const productsWithImages = await Promise.all(
            productList.map(async (product) => {
                try {
                    const imagesResponse = await productService.getProductImages(product.id);
                    return { ...product, images: extractList(imagesResponse) };
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

    } catch (error) {
        console.error('Initialization error:', error);
        alert('Could not initialize the page correctly.');
    } finally {
      setLoadingProducts(false);
    }
  }, [userId]);

  const loadProductImages = async (product) => {
    try {
      setSelectedProduct(product);

      const imagesResponse = await productService.getProductImages(product.id);
      const images = extractList(imagesResponse);

      setSelectedProductImages(images);

      setProducts((prev) =>
        prev.map((item) =>
          item.id === product.id
            ? {
                ...item,
                images,
              }
            : item
        )
      );
    } catch (error) {
      console.error('Load product images error:', error);
      alert(getErrorMessage(error, 'Could not load product images.'));
    }
  };

  const fetchOrders = useCallback(async () => {
    try {
        setLoadingOrders(true);
        const response = await orderService.getOrdersForSeller();
        setOrders(response.data || response || []);
    } catch (error) {
        console.error("Error fetching orders:", error);
    } finally {
        setLoadingOrders(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'products') {
        loadData().catch(console.error);
    } else if (activeTab === 'orders') {
        fetchOrders();
    }
  }, [loadData, userId, activeTab, fetchOrders]);

  const handleProductChange = (e) => {
    const { name, value, files } = e.target;

    if (name === 'images') {
      setProductForm((prev) => ({
        ...prev,
        images: Array.from(files || []),
      }));
      return;
    }

    setProductForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleImageChange = (e) => {
    setImageForm({
      file: e.target.files?.[0] || null,
    });
  };

  const generateSlugFromTitle = (title) => {
    return title
      .toLowerCase()
      .trim()
      .replace(/[ăâ]/g, 'a')
      .replace(/[î]/g, 'i')
      .replace(/[șş]/g, 's')
      .replace(/[țţ]/g, 't')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  };

  const handleGenerateSlug = () => {
    setProductForm((prev) => ({
      ...prev,
      slug: generateSlugFromTitle(prev.title),
    }));
  };

  const handleCreateProduct = async (e) => {
    e.preventDefault();
    const submittedFiles = Array.from(e.currentTarget.elements.images?.files || []);

    if (!productForm.categoryId) {
      alert('Please choose a category.');
      return;
    }

    try {
      setCreating(true);

      const payload = {
        title: productForm.title,
        slug: productForm.slug || generateSlugFromTitle(productForm.title),
        description: productForm.description,
        price: Number(productForm.price),
        currency: productForm.currency,
        stock: Number(productForm.stock),
        categoryId: productForm.categoryId,
      };

      const createResponse = await productService.createProduct(payload);
      const createdProduct = getCreatedProductFromResponse(createResponse);

      if (!createdProduct?.id) {
        console.error('Create product response:', createResponse);
        throw new Error('The product was created, but no product ID was returned.');
      }

      if (submittedFiles.length > 0) {
        for (let i = 0; i < submittedFiles.length; i += 1) {
          await productService.addProductImage(
            createdProduct.id,
            submittedFiles[i],
            '',
            i
          );
        }
      }

      setProductForm({
        title: '',
        slug: '',
        description: '',
        price: '',
        currency: 'RON',
        stock: '',
        categoryId: categories[0]?.id || '',
        images: [],
      });

      e.target.reset();

      await loadData();
      alert('Product added successfully.');
    } catch (error) {
      console.error('Create product error:', error);
      console.error('Response:', error?.response?.data);

      alert(
        getErrorMessage(
          error,
          'Could not create the product. Please check the entered data.'
        )
      );
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteProduct = async (id) => {
    const confirmed = window.confirm('Are you sure you want to delete this product?');
    if (!confirmed) return;

    try {
      await productService.deleteProduct(id);

      setProducts((prev) => prev.filter((product) => product.id !== id));

      if (selectedProduct?.id === id) {
        setSelectedProduct(null);
        setSelectedProductImages([]);
      }

      alert('Product deleted.');
    } catch (error) {
      console.error('Delete product error:', error);
      alert(getErrorMessage(error, 'Could not delete the product.'));
    }
  };

  const handleUpdateStock = async (id) => {
    const stock = window.prompt('Enter the new stock value:');
    if (stock === null) return;

    try {
      const response = await productService.updateStock(id, Number(stock));
      const updatedProduct = getProductFromResponse(response);

      setProducts((prev) =>
        prev.map((product) =>
          product.id === id
            ? {
                ...product,
                ...(updatedProduct || {}),
                images: product.images || [],
              }
            : product
        )
      );

      if (selectedProduct?.id === id && updatedProduct) {
        setSelectedProduct((prev) => ({
          ...prev,
          ...updatedProduct,
        }));
      }

      alert('Stock updated.');
    } catch (error) {
      console.error('Update stock error:', error);
      alert(getErrorMessage(error, 'Could not update the stock.'));
    }
  };

  const handleUpdatePrice = async (id) => {
    const price = window.prompt('Enter the new price:');
    if (price === null) return;

    try {
      const response = await productService.updatePrice(id, Number(price));
      const updatedProduct = getProductFromResponse(response);

      setProducts((prev) =>
        prev.map((product) =>
          product.id === id
            ? {
                ...product,
                ...(updatedProduct || {}),
                images: product.images || [],
              }
            : product
        )
      );

      if (selectedProduct?.id === id && updatedProduct) {
        setSelectedProduct((prev) => ({
          ...prev,
          ...updatedProduct,
        }));
      }

      alert('Price updated.');
    } catch (error) {
      console.error('Update price error:', error);
      alert(getErrorMessage(error, 'Could not update the price.'));
    }
  };

  const handleActivate = async (id) => {
    try {
      const response = await productService.activateProduct(id);
      const updatedProduct = getProductFromResponse(response);

      setProducts((prev) =>
        prev.map((product) =>
          product.id === id
            ? {
                ...product,
                ...(updatedProduct || {}),
                images: product.images || [],
              }
            : product
        )
      );

      if (selectedProduct?.id === id && updatedProduct) {
        setSelectedProduct((prev) => ({
          ...prev,
          ...updatedProduct,
        }));
      }

      alert('Product activated.');
    } catch (error) {
      console.error('Activate product error:', error);
      alert(getErrorMessage(error, 'Could not activate the product.'));
    }
  };

  const handleDeactivate = async (id) => {
    try {
      const response = await productService.deactivateProduct(id);
      const updatedProduct = getProductFromResponse(response);

      setProducts((prev) =>
        prev.map((product) =>
          product.id === id
            ? {
                ...product,
                ...(updatedProduct || {}),
                images: product.images || [],
              }
            : product
        )
      );

      if (selectedProduct?.id === id && updatedProduct) {
        setSelectedProduct((prev) => ({
          ...prev,
          ...updatedProduct,
        }));
      }

      alert('Product deactivated.');
    } catch (error) {
      console.error('Deactivate product error:', error);
      alert(getErrorMessage(error, 'Could not deactivate the product.'));
    }
  };

  const handleAddImage = async (e) => {
    e.preventDefault();

    if (!selectedProduct) {
      alert('Please select a product first.');
      return;
    }

    if (!imageForm.file) {
      alert('Please choose an image.');
      return;
    }

    try {
      setUploadingImage(true);

      await productService.addProductImage(
        selectedProduct.id,
        imageForm.file,
        '',
        selectedProductImages.length
      );

      setImageForm({
        file: null,
      });

      e.target.reset();

      await loadProductImages(selectedProduct);

      alert('Image added.');
    } catch (error) {
      console.error('Add image error:', error);
      console.error('Response:', error?.response?.data);

      alert(getErrorMessage(error, 'Could not add the image.'));
    } finally {
      setUploadingImage(false);
    }
  };

  const handleDeleteImage = async (imageId) => {
    if (!selectedProduct) return;

    const confirmed = window.confirm('Are you sure you want to delete this image?');
    if (!confirmed) return;

    try {
      await productService.deleteProductImage(selectedProduct.id, imageId);

      const updatedImages = selectedProductImages.filter((image) => image.id !== imageId);
      setSelectedProductImages(updatedImages);

      setProducts((prev) =>
        prev.map((product) =>
          product.id === selectedProduct.id
            ? {
                ...product,
                images: updatedImages,
              }
            : product
        )
      );

      alert('Image deleted.');
    } catch (error) {
      console.error('Delete image error:', error);
      alert(getErrorMessage(error, 'Could not delete the image.'));
    }
  };

  const handleOrderStatusChange = async (orderId, newStatus) => {
    try {
        await orderService.updateOrderStatusSeller(orderId, newStatus);
        alert(`Order status updated to ${newStatus}`);
        fetchOrders(); // Refresh the list
    } catch (error) {
        alert("Failed to update status: " + error.message);
    }
  };

  const handleViewOrderDetails = async (orderId) => {
    try {
        const response = await orderService.getOrderById(orderId);
        setSelectedOrder(response.data || response);
    } catch (error) {
        alert("Failed to load order details.");
        console.error(error);
    }
  };

  const closeOrderModal = () => {
    setSelectedOrder(null);
  };

  const tabStyle = (isActive) => ({
    padding: '1rem 2rem',
    border: 'none',
    borderBottom: isActive ? '3px solid #f39c12' : '3px solid transparent',
    background: 'transparent',
    cursor: 'pointer',
    fontWeight: isActive ? 700 : 500,
    fontSize: '1.1rem',
    color: isActive ? '#f39c12' : '#6c757d',
    transition: 'all 0.2s ease',
    marginBottom: '20px'
  });

    // --- If profile is missing, show a message ---
  if (isProfileMissing) {
    return (
        <div className="seller-products-page">
            <div className="seller-products-container">
                <MainNavbar />
                <div style={{ textAlign: 'center', marginTop: '100px', background: '#fff', padding: '40px', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)' }}>
                    <h2 style={{ color: '#e74c3c' }}>Profile Setup Required</h2>
                    <p style={{ fontSize: '1.1rem', color: '#555', margin: '20px 0' }}>
                        You must set up your Shop Profile (Name, Description, Logo) before you can add or manage products.
                    </p>
                    <Link to="/seller" style={{ padding: '10px 20px', background: '#3498db', color: '#fff', textDecoration: 'none', borderRadius: '5px', fontWeight: 'bold' }}>
                        Go to Shop Settings
                    </Link>
                </div>
            </div>
        </div>
    );
  }

  return (
    <div className="seller-products-page">
      <div className="seller-products-container" style={{ maxWidth: '1400px', margin: '0 auto', padding: '0 20px' }}>
        <MainNavbar pageTitle="Seller Dashboard" />

        <div style={{ display: 'flex', gap: '1rem', borderBottom: '1px solid #ddd', marginBottom: '20px' }}>
            <button style={tabStyle(activeTab === 'products')} onClick={() => setActiveTab('products')}>
                My Products
            </button>
            <button style={tabStyle(activeTab === 'orders')} onClick={() => setActiveTab('orders')}>
                Received Orders
            </button>
        </div>

        {activeTab === 'products' && (
            <>
                <section className="seller-products-header-card">
                  <div>
                    <span className="seller-products-eyebrow">Inventory</span>
                    <h2>My products</h2>
                    <p>
                      Add products, upload images, and manage stock and pricing for your listings.
                    </p>
                  </div>

          <button type="button" className="seller-products-refresh" onClick={loadData}>
            Refresh
          </button>
        </section>

                <form className="seller-product-form" onSubmit={handleCreateProduct}>
                  <h3>Add new product</h3>

                  <div className="seller-form-grid">
                    <div className="seller-form-field seller-form-field-wide">
                      <label>Product title</label>
                      <input
                        name="title"
                        placeholder="e.g. Lenovo IdeaPad Laptop"
                        value={productForm.title}
                        onChange={handleProductChange}
                        required
                      />
                    </div>

                    <div className="seller-form-field seller-form-field-wide">
                      <label>Slug</label>
                      <div className="seller-slug-row">
                        <input
                          name="slug"
                          placeholder="e.g. lenovo-ideapad-laptop"
                          value={productForm.slug}
                          onChange={handleProductChange}
                          required
                        />

                        <button type="button" onClick={handleGenerateSlug}>
                          Generate
                        </button>
                      </div>
                    </div>

                    <div className="seller-form-field seller-form-field-wide">
                      <label>Description</label>
                      <textarea
                        name="description"
                        placeholder="Product description"
                        value={productForm.description}
                        onChange={handleProductChange}
                      />
                    </div>

                    <div className="seller-form-field">
                      <label>Price</label>
                      <input
                        name="price"
                        type="number"
                        step="0.01"
                        min="0"
                        placeholder="2500"
                        value={productForm.price}
                        onChange={handleProductChange}
                        required
                      />
                    </div>

                    <div className="seller-form-field">
                      <label>Currency</label>
                      <input
                        name="currency"
                        placeholder="RON"
                        value={productForm.currency}
                        onChange={handleProductChange}
                        required
                        maxLength={3}
                      />
                    </div>

                    <div className="seller-form-field">
                      <label>Stock</label>
                      <input
                        name="stock"
                        type="number"
                        min="0"
                        placeholder="10"
                        value={productForm.stock}
                        onChange={handleProductChange}
                        required
                      />
                    </div>

                    <div className="seller-form-field">
                      <label>Category</label>
                      <select
                        name="categoryId"
                        value={productForm.categoryId}
                        onChange={handleProductChange}
                        required
                      >
                        <option value="">Choose category</option>

                        {categories.map((category) => (
                          <option key={category.id} value={category.id}>
                            {category.name || category.title}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="seller-form-field seller-form-field-wide">
                      <label>Product images</label>
                      <input
                        name="images"
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        multiple
                        onChange={handleProductChange}
                      />

                      {productForm.images.length > 0 && (
                        <small>
                          You selected {productForm.images.length} image(s).
                        </small>
                      )}
                    </div>
                  </div>

                  {categories.length === 0 && (
                    <div className="seller-warning">
                      No categories exist yet. Categories can be created by an admin or directly in the database.
                    </div>
                  )}

                  <button
                    className="seller-submit-btn"
                    type="submit"
                    disabled={creating || categories.length === 0}
                  >
                    {creating ? 'Adding product and images...' : 'Add product'}
                  </button>
                </form>

                <section className="seller-products-list-section">
                  <div className="seller-section-title-row">
                    <div>
                      <h3>My product list</h3>
                      <p>{products.length} uploaded products</p>
                    </div>
                  </div>

                  {loadingProducts && <p className="seller-empty-text">Loading products...</p>}

                  {!loadingProducts && products.length === 0 && (
                    <p className="seller-empty-text">You have not added any products yet.</p>
                  )}

                  <div className="seller-products-grid">
                    {products.map((product) => (
                      <article className="seller-product-card" key={product.id}>
                        <div className="seller-product-image">
                          {product.images?.length > 0 && product.images[0]?.url ? (
                            <img
                              src={getProductImageUrl(product.images[0].url)}
                              alt={product.title}
                            />
                          ) : (
                            <span>No image</span>
                          )}
                        </div>

                        <div className="seller-product-body">
                          <div className="seller-product-title-row">
                            <h4>{product.title}</h4>
                            <span className={`seller-status ${String(product.status || '').toLowerCase()}`}>
                              {product.status || 'N/A'}
                            </span>
                          </div>

                          <p className="seller-product-description">
                            {product.description || 'No description'}
                          </p>

                          <div className="seller-product-meta">
                            <strong>
                              {product.price} {product.currency || 'RON'}
                            </strong>

                            <span>Stock: {product.stock}</span>

                            <span style={{ marginLeft: '10px', color: '#f39c12', fontWeight: 'bold' }}>
                                ★ {product.avgRating ?? product.rating ?? '0.0'} ({product.reviewCount || 0})
                            </span>
                          </div>

                          <p className="seller-product-slug">{product.slug}</p>

                          <div className="seller-product-actions">
                            <button type="button" onClick={() => navigate(`/product/${product.id}`)} style={{ backgroundColor: '#f39c12', color: 'white', border: 'none' }}>
                              View & Reply Reviews
                            </button>

                            <button type="button" onClick={() => loadProductImages(product)}>
                              Images
                            </button>

                            <button type="button" onClick={() => handleUpdateStock(product.id)}>
                              Stock
                            </button>

                            <button type="button" onClick={() => handleUpdatePrice(product.id)}>
                              Price
                            </button>

                            <button type="button" onClick={() => handleActivate(product.id)}>
                              Activate
                            </button>

                            <button type="button" onClick={() => handleDeactivate(product.id)}>
                              Deactivate
                            </button>

                            <button
                              type="button"
                              className="danger"
                              onClick={() => handleDeleteProduct(product.id)}
                            >
                              Delete
                            </button>
                          </div>
                        </div>
                      </article>
                    ))}
                  </div>
                </section>

                {selectedProduct && (
                  <section className="seller-product-details">
                    <div className="seller-section-title-row">
                      <div>
                        <h3>Product images: {selectedProduct.title}</h3>
                        <p>You can add or delete images for the selected product.</p>
                      </div>

                      <button
                        type="button"
                        className="seller-close-details"
                        onClick={() => {
                          setSelectedProduct(null);
                          setSelectedProductImages([]);
                        }}
                      >
                        Close
                      </button>
                    </div>

                    <form className="seller-add-image-form" onSubmit={handleAddImage}>
                      <input
                        name="file"
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        onChange={handleImageChange}
                        required
                      />

                      <button type="submit" disabled={uploadingImage}>
                        {uploadingImage ? 'Uploading...' : 'Add image'}
                      </button>
                    </form>

                    {selectedProductImages.length === 0 && (
                      <p className="seller-empty-text">This product has no images.</p>
                    )}

                    <div className="seller-images-grid">
                      {selectedProductImages.map((image) => (
                        <article className="seller-image-card" key={image.id}>
                          {image.url && (
                            <img
                              src={getProductImageUrl(image.url)}
                              alt="Product"
                            />
                          )}

                          <button
                            type="button"
                            onClick={() => handleDeleteImage(image.id)}
                          >
                            Delete image
                          </button>
                        </article>
                      ))}
                    </div>
                  </section>
                )}
            </>
        )}

        {activeTab === 'orders' && (
            <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)', padding: '2rem', minHeight: '60vh' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                    <div>
                        <h2 style={{ color: '#2c3e50', margin: 0 }}>Received Orders</h2>
                        <p style={{ color: '#555', fontSize: '1.05rem', margin: 0 }}>
                            Manage the orders that contain your products.
                        </p>
                    </div>
                    <button className="btn btn-outline-secondary" onClick={fetchOrders}>
                        Refresh List
                    </button>
                </div>

                {loadingOrders ? (
                    <p>Loading orders...</p>
                ) : orders.length === 0 ? (
                    <p>You have not received any orders yet.</p>
                ) : (
                    <div className="table-responsive">
                        <table className="table table-bordered table-hover">
                            <thead className="table-light">
                                <tr>
                                    <th>Order ID</th>
                                    <th>Date</th>
                                    <th>Your Products</th>
                                    <th>Delivery Address</th>
                                    <th>Order Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {orders.map(order => (
                                    <tr key={order.id}>
                                        <td><small>{order.id}</small></td>
                                        <td>{new Date(order.placedAt).toLocaleString()}</td>
                                        <td>
                                            <ul style={{ paddingLeft: '20px', margin: 0 }}>
                                                {order.items?.map(item => (
                                                    <li key={item.productId}>
                                                        {item.productTitle} (x{item.quantity})
                                                    </li>
                                                ))}
                                            </ul>
                                        </td>
                                        <td>{order.deliveryAddress}</td>
                                        <td>
                                            <span className={`badge bg-${order.status === 'PENDING' ? 'warning' : order.status === 'CANCELLED' ? 'danger' : 'success'}`}>
                                                {order.status}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="d-flex gap-2 flex-column">
                                                <button
                                                    className="btn btn-sm btn-outline-primary"
                                                    onClick={() => {
                                                        setSelectedOrder(order);
                                                    }}
                                                >
                                                    View Details
                                                </button>
                                                <select
                                                    className="form-select form-select-sm"
                                                    value={order.status}
                                                    onChange={(e) => handleOrderStatusChange(order.id, e.target.value)}
                                                    disabled={order.status === 'CANCELLED' || order.status === 'DELIVERED'}
                                                >
                                                    <option value="PENDING">PENDING</option>
                                                    <option value="PROCESSING">PROCESSING</option>
                                                    <option value="SHIPPED">SHIPPED</option>
                                                    <option value="DELIVERED">DELIVERED</option>
                                                    <option value="CANCELLED">CANCELLED</option>
                                                </select>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        )}

        {/* ORDER DETAILS MODAL FOR SELLER */}
        {selectedOrder && activeTab === 'orders' && (
            <div className="modal" style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }} tabIndex="-1">
                <div className="modal-dialog modal-lg">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">Order Details</h5>
                            <button type="button" className="btn-close" onClick={closeOrderModal}></button>
                        </div>
                        <div className="modal-body">
                            <div className="mb-3">
                                <strong>Order ID:</strong> {selectedOrder.id} <br/>
                                <strong>Customer ID:</strong> {selectedOrder.customerId} <br/>
                                <strong>Overall Status:</strong> <span className={`badge bg-${selectedOrder.status === 'PENDING' ? 'warning' : selectedOrder.status === 'CANCELLED' ? 'danger' : 'success'}`}>{selectedOrder.status}</span> <br/>
                                <strong>Date:</strong> {new Date(selectedOrder.placedAt).toLocaleString()} <br/>
                                <strong>Delivery Address:</strong> {selectedOrder.deliveryAddress}
                            </div>

                            <h6>Your Items in this order:</h6>
                            <table className="table table-sm table-striped">
                                <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>Price</th>
                                        <th>Qty</th>
                                        <th>Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {selectedOrder.items?.map(item => (
                                        <tr key={item.productId}>
                                            <td>{item.productTitle} <br/><small className="text-muted">{item.productId}</small></td>
                                            <td>${item.unitPrice.toFixed(2)}</td>
                                            <td>{item.quantity}</td>
                                            <td>${(item.unitPrice * item.quantity).toFixed(2)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>

                            <div className="alert alert-info mt-3 py-2">
                                <small>Note: This order may contain items from other sellers. The items listed above are only the ones provided by you.</small>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" onClick={closeOrderModal}>Close</button>
                        </div>
                    </div>
                </div>
            </div>
        )}
      </div>
    </div>
  );
};

export default SellerProductsPage;
