-- Initial E-Commerce Schema
-- This migration captures the baseline schema for the production database

-- =====================================================================
-- BASE TABLES
-- =====================================================================

-- Users table with authentication, roles, and seller information
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255),
    phone VARCHAR(20),
    status VARCHAR(50),
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    auth_provider VARCHAR(50) NOT NULL,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME,
    seller_status VARCHAR(30) NOT NULL DEFAULT 'NOT_A_SELLER',
    seller_requested_at DATETIME,
    seller_approved_at DATETIME,
    seller_rejection_reason VARCHAR(500),
    approved_by_admin_id BIGINT,
    CONSTRAINT fk_user_admin FOREIGN KEY (approved_by_admin_id) REFERENCES users(id),
    INDEX idx_user_email (email),
    INDEX idx_user_status (status),
    INDEX idx_user_seller_status (seller_status)
);

-- User roles collection table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50),
    CONSTRAINT fk_user_roles FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seller applications with business and tax details
CREATE TABLE seller_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(100),
    business_description VARCHAR(1000),
    pan VARCHAR(50),
    gst_number VARCHAR(50),
    tax_id VARCHAR(100),
    business_address_line1 VARCHAR(255),
    business_address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    phone_number VARCHAR(20),
    bank_name VARCHAR(255),
    account_holder_name VARCHAR(255),
    account_number VARCHAR(50),
    ifsc_code VARCHAR(20),
    swift_code VARCHAR(20),
    pan_document_url VARCHAR(500),
    gst_document_url VARCHAR(500),
    business_proof_url VARCHAR(500),
    bank_proof_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    submission_date DATETIME,
    review_date DATETIME,
    rejection_reason VARCHAR(1000),
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_seller_app_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_seller_app_user_id (user_id),
    INDEX idx_seller_app_status (status)
);

-- User addresses
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    line1 VARCHAR(255),
    line2 VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    postal_code VARCHAR(255),
    user_id BIGINT,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================================================
-- PRODUCT CATALOG TABLES
-- =====================================================================

-- Brand master
CREATE TABLE brands (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(500),
    country VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
);

-- Category master
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
);

-- Products with multi-vendor support
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    price_amount DECIMAL(38, 2),
    price_currency VARCHAR(3),
    sku VARCHAR(100) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    seller_user_id BIGINT,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brands(id),
    CONSTRAINT fk_product_seller FOREIGN KEY (seller_user_id) REFERENCES users(id),
    INDEX idx_product_sku (sku),
    INDEX idx_product_status (status),
    INDEX idx_product_category (category_id),
    INDEX idx_product_brand (brand_id),
    INDEX idx_product_created_at (created_at)
);

-- Product inventory
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT UNIQUE,
    quantity INT,
    version BIGINT,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- =====================================================================
-- SHOPPING CART TABLES
-- =====================================================================

-- Shopping cart
CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Cart items
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    price_amount DECIMAL(38, 2),
    price_currency VARCHAR(3),
    quantity INT NOT NULL,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uk_cart_product UNIQUE KEY (cart_id, product_id)
);

-- =====================================================================
-- ORDER TABLES
-- =====================================================================

-- Orders
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    total_amount_amount DECIMAL(38, 2),
    total_amount_currency VARCHAR(3),
    status VARCHAR(50),
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_order_user (user_id),
    INDEX idx_order_status (status),
    INDEX idx_order_created_at (created_at)
);

-- Order items
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_amount DECIMAL(38, 2),
    price_currency VARCHAR(3),
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- =====================================================================
-- PAYMENT TABLES
-- =====================================================================

-- Payments
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount_amount DECIMAL(38, 2),
    amount_currency VARCHAR(3),
    transaction_id VARCHAR(255),
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- =====================================================================
-- AUTHENTICATION TABLES
-- =====================================================================

-- Refresh tokens for JWT authentication
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(512) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME,
    created_by VARCHAR(255),
    updated_at DATETIME,
    updated_by VARCHAR(255)
);
