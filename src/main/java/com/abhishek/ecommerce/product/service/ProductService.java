package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    
    // CREATE
    ProductResponseDto createProduct(ProductCreateRequestDto requestDto);
    
    // READ
    ProductResponseDto getProductById(Long productId);
    List<ProductResponseDto> getAllProducts();
    PageResponseDto<ProductResponseDto> getAllProducts(Pageable pageable);
    List<ProductResponseDto> getAllActiveProducts();
    PageResponseDto<ProductResponseDto> getAllActiveProducts(Pageable pageable);
    
    // Filtering methods
    PageResponseDto<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable);
    PageResponseDto<ProductResponseDto> getProductsByBrand(Long brandId, Pageable pageable);
    PageResponseDto<ProductResponseDto> getProductsByCategoryAndBrand(Long categoryId, Long brandId, Pageable pageable);
    PageResponseDto<ProductResponseDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    PageResponseDto<ProductResponseDto> searchProductsByName(String name, Pageable pageable);
    PageResponseDto<ProductResponseDto> searchActiveProductsByName(String name, Pageable pageable);
    
    // UPDATE
    ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto requestDto);
    
    // STATUS OPERATIONS
    void activateProduct(Long productId);
    void deactivateProduct(Long productId);
    
    // DELETE (soft delete)
    void deleteProduct(Long productId);

    // COUNT OPERATIONS
    long getTotalProductCount();

    // SELLER OPERATIONS
    /**
     * Check if a seller owns a product
     */
    boolean isSellerOwner(Long productId, Long sellerId);

    /**
     * Get all products for a specific seller
     */
    List<ProductResponseDto> getProductsBySeller(Long sellerId);

    /**
     * Get all active products for a specific seller
     */
    List<ProductResponseDto> getActiveProductsBySeller(Long sellerId);}