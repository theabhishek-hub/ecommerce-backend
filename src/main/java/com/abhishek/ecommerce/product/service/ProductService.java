package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;

import java.util.List;

public interface ProductService {
    
    // CREATE
    ProductResponseDto createProduct(ProductCreateRequestDto requestDto);
    
    // READ
    ProductResponseDto getProductById(Long productId);
    List<ProductResponseDto> getAllProducts();
    List<ProductResponseDto> getAllActiveProducts();
    
    // UPDATE
    ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto requestDto);
    
    // STATUS OPERATIONS
    void activateProduct(Long productId);
    void deactivateProduct(Long productId);
    
    // DELETE (soft delete)
    void deleteProduct(Long productId);
}

