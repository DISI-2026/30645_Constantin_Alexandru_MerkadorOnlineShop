-- ============================================================
-- V1__initial_schema.sql
-- Product Service - Initial Database Schema
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- CATEGORIES
-- Self-referencing tree via parent_id (adjacency list)
-- Root categories have parent_id = null
-- ============================================================
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    parent_id   UUID REFERENCES categories(id) ON DELETE SET NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_is_active ON categories(is_active);

-- ============================================================
-- PRODUCTS
-- vendor_id and category_id are logical (cross-service) FKs
-- avg_rating / review_count are denormalized aggregates
-- ============================================================
CREATE TABLE products (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vendor_id       UUID NOT NULL,
    category_id     UUID NOT NULL REFERENCES categories(id),
    title           VARCHAR(500) NOT NULL,
    slug            VARCHAR(500) NOT NULL UNIQUE,
    description     TEXT,
    price           NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    currency        VARCHAR(3) NOT NULL DEFAULT 'RON',
    stock           INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    status          VARCHAR(50) NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'DELETED')),
    avg_rating      NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    review_count    INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_vendor_id ON products(vendor_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_avg_rating ON products(avg_rating);
CREATE INDEX idx_products_created_at ON products(created_at DESC);

-- ============================================================
-- PRODUCT_IMAGES
-- Separate collection for query flexibility
-- ============================================================
CREATE TABLE product_images (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url         VARCHAR(1000) NOT NULL,
    alt_text    VARCHAR(500),
    sort_order  INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);

-- ============================================================
-- PRODUCT_VARIANTS
-- Allows size/color variants with individual stock and price modifier
-- ============================================================
CREATE TABLE product_variants (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku             VARCHAR(255) NOT NULL UNIQUE,
    label           VARCHAR(255) NOT NULL,
    price_modifier  NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    stock           INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);

-- ============================================================
-- ES_INDEX_OUTBOX
-- Ensures Elasticsearch stays in sync via outbox pattern
-- Processed by a background job that calls the ES API
-- ============================================================
CREATE TABLE es_index_outbox (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    operation       VARCHAR(50) NOT NULL CHECK (operation IN ('UPSERT', 'DELETE')),
    processed       BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count     INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_es_outbox_processed ON es_index_outbox(processed);
CREATE INDEX idx_es_outbox_product_id ON es_index_outbox(product_id);
CREATE INDEX idx_es_outbox_created_at ON es_index_outbox(created_at ASC);

-- ============================================================
-- TRIGGER: auto-update updated_at on products
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
