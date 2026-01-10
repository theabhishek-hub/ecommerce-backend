-- V7__seed_initial_data.sql
-- Seed initial data for the application

-- Insert basic categories
INSERT INTO categories (name, description, status, created_at, updated_at) VALUES
('Electronics', 'Electronic devices and gadgets', 'ACTIVE', NOW(), NOW()),
('Clothing', 'Apparel and fashion items', 'ACTIVE', NOW(), NOW()),
('Books', 'Books and publications', 'ACTIVE', NOW(), NOW()),
('Home & Garden', 'Home improvement and garden supplies', 'ACTIVE', NOW(), NOW()),
('Sports', 'Sports equipment and apparel', 'ACTIVE', NOW(), NOW()),
('Beauty', 'Beauty and personal care products', 'ACTIVE', NOW(), NOW());

-- Insert basic brands
INSERT INTO brands (name, description, country, status, created_at, updated_at) VALUES
('Apple', 'Technology company', 'USA', 'ACTIVE', NOW(), NOW()),
('Samsung', 'Electronics manufacturer', 'South Korea', 'ACTIVE', NOW(), NOW()),
('Nike', 'Sportswear company', 'USA', 'ACTIVE', NOW(), NOW()),
('Adidas', 'Sportswear brand', 'Germany', 'ACTIVE', NOW(), NOW()),
('Sony', 'Entertainment and technology company', 'Japan', 'ACTIVE', NOW(), NOW()),
('Generic', 'Generic brand for various products', 'Unknown', 'ACTIVE', NOW(), NOW());

-- Insert default admin user
-- Password hash for 'admin123' using BCrypt (you may want to change this)
INSERT INTO users (email, password_hash, full_name, status, role, auth_provider, failed_login_attempts, created_at, updated_at) VALUES
('admin@example.com', '$2a$10$example.hash.for.admin123', 'System Administrator', 'ACTIVE', 'ROLE_ADMIN', 'LOCAL', 0, NOW(), NOW());