package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.request.SellerProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.product.service.ImageUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Tag(name = "Products", description = "Product APIs (ADMIN/SELLER manage, USER read)")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ImageUploadService imageUploadService;

    // ========================= CREATE PRODUCT (WITH IMAGE UPLOAD) =========================
    /**
     * Create product with optional image upload.
     * Multipart endpoint: POST with individual fields + images (1-2, JPG/PNG, max 1MB each)
     */
    @Operation(
        summary = "Create product",
        description = "Create product with optional image upload (1-2 images: JPG/PNG, max 1MB each). Requires ADMIN or SELLER role."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public Object createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("priceAmount") String priceAmount,
            @RequestParam("currency") String currency,
            @RequestParam("sku") String sku,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("brandId") Long brandId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            log.info("Creating product with images");

            // If no images provided, create without images
            if (images == null || images.isEmpty()) {
                log.info("Creating product without images");
                ProductCreateRequestDto requestDto = new ProductCreateRequestDto();
                requestDto.setName(name);
                requestDto.setDescription(description);
                requestDto.setPriceAmount(new BigDecimal(priceAmount));
                requestDto.setCurrency(currency);
                requestDto.setSku(sku);
                requestDto.setCategoryId(categoryId);
                requestDto.setBrandId(brandId);
                
                ProductResponseDto response = productService.createProduct(requestDto);
                return ApiResponseBuilder.created("Product created successfully", response);
            }

            // Validate image count
            if (images.size() > 2) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "Maximum 2 images allowed");
            }

            // Remove empty files
            images = images.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList();

            if (images.isEmpty()) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "At least 1 valid image is required");
            }

            if (images.size() > 2) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "Maximum 2 images allowed");
            }

            // Validate each image
            for (MultipartFile image : images) {
                validateImageFile(image);
            }

            // Upload images
            List<String> imageUrls = imageUploadService.uploadImages(images);
            String primaryImageUrl = imageUrls.get(0); // Use first image as primary

            log.info("Images uploaded successfully: {} images", imageUrls.size());

            // Create product DTO and set images
            SellerProductCreateRequestDto sellerDto = new SellerProductCreateRequestDto();
            sellerDto.setName(name);
            sellerDto.setDescription(description);
            sellerDto.setPriceAmount(new BigDecimal(priceAmount));
            sellerDto.setCurrency(currency);
            sellerDto.setSku(sku);
            sellerDto.setCategoryId(categoryId);
            sellerDto.setBrandId(brandId);

            // Convert to standard DTO and create product
            ProductCreateRequestDto productDto = sellerDto.toProductCreateRequestDto(primaryImageUrl);
            ProductResponseDto response = productService.createProduct(productDto);

            log.info("Product created successfully with {} image(s). ProductId={}", imageUrls.size(), response.getId());
            return ApiResponseBuilder.created("Product created with " + imageUrls.size() + " image(s)", response);

        } catch (IllegalArgumentException e) {
            log.warn("Image validation failed: {}", e.getMessage());
            return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating product with images", e);
            return ApiResponseBuilder.failed(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create product: " + e.getMessage());
        }
    }

    // ========================= VALIDATE IMAGES =========================
    @Operation(
        summary = "Validate product images",
        description = "Validate image files without uploading them"
    )
    @PostMapping(value = "/validate-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Object validateImages(
            @RequestParam("images") List<MultipartFile> images
    ) {
        try {
            if (images == null || images.isEmpty()) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "At least 1 image is required");
            }

            images = images.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList();

            if (images.isEmpty()) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "At least 1 valid image is required");
            }

            if (images.size() > 2) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "Maximum 2 images allowed. You provided " + images.size());
            }

            // Validate each image
            for (MultipartFile image : images) {
                validateImageFile(image);
            }

            log.info("Image validation passed for {} images", images.size());
            return ApiResponseBuilder.success("All images are valid", "OK");

        } catch (IllegalArgumentException e) {
            log.warn("Image validation failed: {}", e.getMessage());
            return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Validate a single image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file format: " + file.getOriginalFilename() + 
                    ". Only JPG and PNG are allowed"
            );
        }

        long maxSize = 1024 * 1024; // 1 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    "File size exceeds 1 MB: " + file.getOriginalFilename() + 
                    " (" + (file.getSize() / 1024) + " KB)"
            );
        }
    }

    // ========================= UPDATE =========================
    @Operation(
        summary = "Update product",
        description = "Requires ADMIN or SELLER role (SELLER: own products only)"
    )
    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER') and (hasRole('ADMIN') or @sellerSecurity.isSellerOwnerProduct(#productId))")
    public ApiResponse<ProductResponseDto> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequestDto requestDto
    ) {
        ProductResponseDto response = productService.updateProduct(productId, requestDto);
        return ApiResponseBuilder.success("Product updated successfully", response);
    }

    // ========================= GET BY ID =========================
    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ProductResponseDto> getProductById(@PathVariable Long productId) {
        ProductResponseDto response = productService.getProductById(productId);
        return ApiResponseBuilder.success("Product fetched successfully", response);
    }

    // ========================= GET ALL =========================
    @Operation(
        summary = "Get all products",
        description = "Retrieves all products (including inactive ones)"
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ApiResponseBuilder.success("All products fetched", products);
    }

    // ========================= GET ALL ACTIVE =========================
    @Operation(
        summary = "Get all active products",
        description = "Retrieves all active products available for purchase"
    )
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ProductResponseDto>> getAllActiveProducts() {
        List<ProductResponseDto> products = productService.getAllActiveProducts();
        return ApiResponseBuilder.success("Active products fetched successfully", products);
    }

    // ========================= GET ALL PAGINATED =========================
    @Operation(
        summary = "Get all products (paginated)",
        description = "Retrieves all products with pagination support"
    )
    @GetMapping("/paged")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getAllProductsPaged(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ProductResponseDto> products = productService.getAllProducts(pageable);
        return ApiResponseBuilder.success("All products fetched", products);
    }

    // ========================= GET ALL ACTIVE PAGINATED =========================
    @Operation(
        summary = "Get all active products (paginated)",
        description = "Retrieves all active products with pagination support"
    )
    @GetMapping("/active/paged")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getAllActiveProductsPaged(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ProductResponseDto> products = productService.getAllActiveProducts(pageable);
        return ApiResponseBuilder.success("Active products fetched successfully", products);
    }

    // ========================= FILTERING ENDPOINTS =========================
    @Operation(
        summary = "Get products by category",
        description = "Retrieves active products filtered by category with pagination"
    )
    @GetMapping("/category/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ProductResponseDto> products = productService.getProductsByCategory(categoryId, pageable);
        return ApiResponseBuilder.success("Products by category fetched successfully", products);
    }

    @Operation(
        summary = "Get products by brand",
        description = "Retrieves active products filtered by brand with pagination"
    )
    @GetMapping("/brand/{brandId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getProductsByBrand(
            @PathVariable Long brandId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ProductResponseDto> products = productService.getProductsByBrand(brandId, pageable);
        return ApiResponseBuilder.success("Products by brand fetched successfully", products);
    }

    @Operation(
        summary = "Filter products",
        description = "Advanced product filtering by category, brand, price range, and name with pagination"
    )
    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getProductsFiltered(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponseDto<ProductResponseDto> products;

        if (categoryId != null && brandId != null) {
            products = productService.getProductsByCategoryAndBrand(categoryId, brandId, pageable);
        } else if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId, pageable);
        } else if (brandId != null) {
            products = productService.getProductsByBrand(brandId, pageable);
        } else if (minPrice != null && maxPrice != null) {
            products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        } else if (name != null && !name.trim().isEmpty()) {
            products = productService.searchActiveProductsByName(name.trim(), pageable);
        } else {
            products = productService.getAllActiveProducts(pageable);
        }

        return ApiResponseBuilder.success("Filtered products fetched successfully", products);
    }

    // ========================= ACTIVATE =========================
    @Operation(
        summary = "Activate product",
        description = "Requires ADMIN or SELLER role (SELLER: own products only)"
    )
    @PutMapping("/{productId}/activate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER') and (hasRole('ADMIN') or @sellerSecurity.isSellerOwnerProduct(#productId))")
    public ApiResponse<Void> activateProduct(@PathVariable Long productId) {
        productService.activateProduct(productId);
        return ApiResponseBuilder.success("Product activated successfully", null);
    }

    // ========================= DEACTIVATE =========================
    @Operation(
        summary = "Deactivate product",
        description = "Requires ADMIN or SELLER role (SELLER: own products only)"
    )
    @PutMapping("/{productId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER') and (hasRole('ADMIN') or @sellerSecurity.isSellerOwnerProduct(#productId))")
    public ApiResponse<Void> deactivateProduct(@PathVariable Long productId) {
        productService.deactivateProduct(productId);
        return ApiResponseBuilder.success("Product deactivated successfully", null);
    }

    // ========================= DELETE (SOFT DELETE) =========================
    @Operation(
        summary = "Delete product",
        description = "Requires ADMIN or SELLER role (SELLER: own products only)"
    )
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER') and (hasRole('ADMIN') or @sellerSecurity.isSellerOwnerProduct(#productId))")
    public ApiResponse<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ApiResponseBuilder.success("Product deleted successfully", null);
    }
}
