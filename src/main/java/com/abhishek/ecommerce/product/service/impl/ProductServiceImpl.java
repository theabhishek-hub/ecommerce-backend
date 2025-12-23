package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product update(Long id, Product product) {
        Product existing = getById(id);
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

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
