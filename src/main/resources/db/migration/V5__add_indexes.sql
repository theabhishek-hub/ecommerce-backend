-- V5__add_indexes.sql
-- Add indexes for performance

-- Indexes for products
CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_status ON products(status);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_brand ON products(brand_id);
CREATE INDEX idx_product_created_at ON products(created_at);

-- Indexes for orders
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);