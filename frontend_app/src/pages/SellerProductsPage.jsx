import React, { useEffect, useState } from 'react';
import MainNavbar from '../components/MainNavbar.jsx';
import { productService, getProductImageUrl } from '../api/productService';
import { categoryService } from '../api/categoryService';
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
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);

  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedProductImages, setSelectedProductImages] = useState([]);

  const [loading, setLoading] = useState(false);
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

  const getErrorMessage = (error, fallback) => {
    return (
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error?.response?.data?.details ||
      error?.message ||
      fallback
    );
  };

  const loadCategories = async () => {
    try {
      const response = await categoryService.getCategories();
      const list = extractList(response);

      setCategories(list);

      if (list.length > 0) {
        setProductForm((prev) => ({
          ...prev,
          categoryId: prev.categoryId || list[0].id,
        }));
      }
    } catch (error) {
      console.error('Categories error:', error);
      alert('Could not load categories.');
    }
  };

  const loadMyProducts = async () => {
    try {
      setLoading(true);

      const response = await productService.getMyProducts({
        page: 0,
        size: 50,
      });

      const productList = extractList(response);

      const productsWithImages = await Promise.all(
        productList.map(async (product) => {
          try {
            const imagesResponse = await productService.getProductImages(product.id);
            const images = extractList(imagesResponse);

            return {
              ...product,
              images,
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
    } catch (error) {
      console.error('My products error:', error);
      alert(
        getErrorMessage(
          error,
          'Could not load your products. Please check that you are logged in as a seller.'
        )
      );
    } finally {
      setLoading(false);
    }
  };

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

  useEffect(() => {
    loadCategories();
    loadMyProducts();
  }, []);

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

      await loadMyProducts();
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

  return (
    <div className="seller-products-page">
      <div className="seller-products-container">
        <MainNavbar />

        <section className="seller-products-header-card">
          <div>
            <span className="seller-products-eyebrow">Seller mode</span>
            <h2>My products</h2>
            <p>
              Add products, upload images, and manage stock and pricing for your listings.
            </p>
          </div>

          <button type="button" className="seller-products-refresh" onClick={loadMyProducts}>
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

          {loading && <p className="seller-empty-text">Loading products...</p>}

          {!loading && products.length === 0 && (
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
                  </div>

                  <p className="seller-product-slug">{product.slug}</p>

                  <div className="seller-product-actions">
                    <button type="button" onClick={() => loadProductImages(product)}>
                      Details
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
      </div>
    </div>
  );
};

export default SellerProductsPage;