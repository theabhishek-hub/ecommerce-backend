package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.entity.ProductStatus;
import com.abhishek.ecommerce.product.exception.ProductAlreadyExistsException;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.mapper.ProductMapper;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.repository.CategoryRepository;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    // ========================= CREATE =========================
    @Override
    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto) {

        // Check duplicate SKU
        if (productRepository.existsBySku(requestDto.getSku())) {
            throw new ProductAlreadyExistsException(
                    "Product already exists with SKU: " + requestDto.getSku()
            );
        }

        Product product = productMapper.toEntity(requestDto);

        // Set category
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + requestDto.getCategoryId()));
        product.setCategory(category);

        // Set brand
        Brand brand = brandRepository.findById(requestDto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + requestDto.getBrandId()));
        product.setBrand(brand);

        product.setStatus(ProductStatus.ACTIVE);

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    // ========================= UPDATE =========================
    @Override
    public ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto requestDto) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // Update only provided fields
        if (requestDto.getName() != null) {
            product.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            product.setDescription(requestDto.getDescription());
        }
        if (requestDto.getPriceAmount() != null && requestDto.getCurrency() != null) {
            product.setPrice(new com.abhishek.ecommerce.common.entity.Money(requestDto.getPriceAmount(), requestDto.getCurrency()));
        }
        if (requestDto.getSku() != null) {
            // Check if SKU is already taken by another product
            productRepository.findBySku(requestDto.getSku())
                    .ifPresent(existingProduct -> {
                        if (!existingProduct.getId().equals(productId)) {
                            throw new ProductAlreadyExistsException("Product already exists with SKU: " + requestDto.getSku());
                        }
                    });
            product.setSku(requestDto.getSku());
        }
        if (requestDto.getImageUrl() != null) {
            product.setImageUrl(requestDto.getImageUrl());
        }
        if (requestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + requestDto.getCategoryId()));
            product.setCategory(category);
        }
        if (requestDto.getBrandId() != null) {
            Brand brand = brandRepository.findById(requestDto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found with id: " + requestDto.getBrandId()));
            product.setBrand(brand);
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllActiveProducts() {
        return productRepository.findAllByStatus(ProductStatus.ACTIVE)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========================= STATUS =========================
    @Override
    @Transactional
    public void activateProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    // ========================= DELETE (SOFT) =========================
    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }
}
