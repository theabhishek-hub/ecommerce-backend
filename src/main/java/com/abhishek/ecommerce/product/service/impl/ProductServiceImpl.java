package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.shared.enums.ProductStatus;
import com.abhishek.ecommerce.product.exception.ProductAlreadyExistsException;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.mapper.ProductMapper;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.repository.CategoryRepository;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    // ========================= CREATE =========================
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto) {
        log.info("createProduct started for sku={}", requestDto.getSku());

        // Check duplicate SKU
        if (productRepository.existsBySku(requestDto.getSku())) {
            log.warn("createProduct duplicate sku={}", requestDto.getSku());
            throw new ProductAlreadyExistsException(requestDto.getSku());
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

        // ✅ ROLE-BASED: Check if current user is SELLER or ADMIN
        // If SELLER: automatically set seller ID from SecurityContext
        // If ADMIN: leave seller as null (global product)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SELLER"))) {
            // Current user is a SELLER - set product owner
            Long sellerId = securityUtils.getCurrentUserId();
            if (sellerId != null) {
                com.abhishek.ecommerce.user.entity.User seller = userRepository.findById(sellerId)
                        .orElseThrow(() -> new RuntimeException("Seller not found with id: " + sellerId));
                product.setSeller(seller);
                log.info("createProduct: Setting seller={} for product sku={}", sellerId, requestDto.getSku());
            }
        } else {
            // Current user is ADMIN - product is global (seller = null)
            log.info("createProduct: Creating global product (admin) sku={}", requestDto.getSku());
        }

        product.setStatus(ProductStatus.ACTIVE);

        Product savedProduct = productRepository.save(product);
        
        // Create initial inventory record for the product with 0 quantity
        try {
            inventoryService.createInitialInventory(savedProduct.getId());
            log.info("Initial inventory created for productId={}", savedProduct.getId());
        } catch (Exception e) {
            log.error("Error creating initial inventory for productId={}", savedProduct.getId(), e);
            // Don't fail the product creation if inventory creation fails
        }

        log.info("createProduct completed for sku={}", requestDto.getSku());
        return productMapper.toDto(savedProduct);
    }

    // ========================= UPDATE =========================
    @Override
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto requestDto) {
        log.info("updateProduct started for productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Update only provided fields
        if (requestDto.getName() != null) {
            product.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            product.setDescription(requestDto.getDescription());
        }
        if (requestDto.getPriceAmount() != null && requestDto.getCurrency() != null) {
            product.setPrice(new com.abhishek.ecommerce.common.baseEntity.Money(requestDto.getPriceAmount(), requestDto.getCurrency()));
        }
        if (requestDto.getSku() != null) {
            // Check if SKU is already taken by another product
            productRepository.findBySku(requestDto.getSku())
                    .ifPresent(existingProduct -> {
                        if (!existingProduct.getId().equals(productId)) {
                            throw new ProductAlreadyExistsException(requestDto.getSku());
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

        log.info("updateProduct completed for productId={}", productId);
        return productMapper.toDto(updatedProduct);
    }

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#productId")
    public ProductResponseDto getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductResponseDto> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getAllActiveProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByStatus(ProductStatus.ACTIVE, pageable);
        List<ProductResponseDto> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    // ========================= FILTERING METHODS =========================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
        List<ProductResponseDto> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getProductsByBrand(Long brandId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByBrandId(brandId, pageable);
        List<ProductResponseDto> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getProductsByCategoryAndBrand(Long categoryId, Long brandId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryIdAndBrandId(categoryId, brandId, pageable);
        List<ProductResponseDto> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> productPage = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        List<ProductResponseDto> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> searchProductsByName(String name, Pageable pageable) {
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        List<ProductResponseDto> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> searchActiveProductsByName(String name, Pageable pageable) {
        Page<Product> productPage = productRepository.findByStatusAndNameContainingIgnoreCase(ProductStatus.ACTIVE, name, pageable);
        List<ProductResponseDto> content = productPage.getContent()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    // ========================= STATUS =========================
    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void activateProduct(Long productId) {
        log.info("activateProduct started for productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);

        log.info("activateProduct completed for productId={}", productId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void deactivateProduct(Long productId) {
        log.info("deactivateProduct started for productId={}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);

        log.info("deactivateProduct completed for productId={}", productId);
    }

    // ========================= DELETE (SOFT) =========================
    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProduct(Long productId) {
        log.info("deleteProduct started for productId={}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
        log.info("deleteProduct completed for productId={}", productId);
    }

    // ========================= COUNT OPERATIONS =========================
    @Override
    public long getTotalProductCount() {
        return productRepository.countByStatus(ProductStatus.ACTIVE);
    }

    // ========================= SELLER OPERATIONS =========================
    @Override
    @Transactional(readOnly = true)
    public boolean isSellerOwner(Long productId, Long sellerId) {
        return productRepository.existsByIdAndSellerId(productId, sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getActiveProductsBySeller(Long sellerId) {
        return productRepository.findBySellerIdAndStatus(sellerId, ProductStatus.ACTIVE)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }
}