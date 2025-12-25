package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.entity.ProductStatus;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product updateProduct(Long productId, Product product) {
        Product existing = getProductById(productId);
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setSku(product.getSku());
        existing.setImageUrl(product.getImageUrl());
        existing.setStatus(product.getStatus());
        existing.setCategory(product.getCategory());
        existing.setBrand(product.getBrand());
        return productRepository.save(existing);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long productId) {
        Product product = getProduct(productId);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void activateProduct(Long productId) {
        Product product = getProduct(productId);
        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override
    public List<Product> getAllActiveProducts() {
        return productRepository.findAllByStatus(ProductStatus.ACTIVE);
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = getProduct(productId);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

}
