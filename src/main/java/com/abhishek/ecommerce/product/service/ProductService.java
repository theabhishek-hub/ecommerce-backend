package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.entity.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product getProductById(Long ProductId);
    List<Product> getAllProducts();
    Product updateProduct(Long ProductId, Product product);

    void deactivateProduct(Long productId);

    void activateProduct(Long productId);

    List<Product> getAllActiveProducts();

    void deleteProduct(Long productId);
}

