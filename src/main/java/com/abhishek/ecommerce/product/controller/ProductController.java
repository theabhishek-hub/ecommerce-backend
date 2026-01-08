package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.common.api.PageResponseDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.util.List;

@Tag(name = "Products", description = "Product APIs (ADMIN manage, USER read)")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ========================= CREATE =========================
    @Operation(
        summary = "Create product",
        description = "Requires ADMIN role"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductCreateRequestDto requestDto
    ) {
        ProductResponseDto response = productService.createProduct(requestDto);
        return ApiResponseBuilder.created("Product created successfully", response);
    }

    // ========================= UPDATE =========================
    @Operation(
        summary = "Update product",
        description = "Requires ADMIN role"
    )
    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
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
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ApiResponseBuilder.success("All products fetched", products);
    }

    // ========================= GET ALL ACTIVE =========================
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ProductResponseDto>> getAllActiveProducts() {
        List<ProductResponseDto> products = productService.getAllActiveProducts();
        return ApiResponseBuilder.success("Active products fetched successfully", products);
    }

    // ========================= GET ALL PAGINATED =========================
    @GetMapping("/paged")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getAllProductsPaged(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ProductResponseDto> products = productService.getAllProducts(pageable);
        return ApiResponseBuilder.success("All products fetched", products);
    }

    // ========================= GET ALL ACTIVE PAGINATED =========================
    @GetMapping("/active/paged")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getAllActiveProductsPaged(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ProductResponseDto> products = productService.getAllActiveProducts(pageable);
        return ApiResponseBuilder.success("Active products fetched successfully", products);
    }

    // ========================= FILTERING ENDPOINTS =========================
    @GetMapping("/category/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ProductResponseDto> products = productService.getProductsByCategory(categoryId, pageable);
        return ApiResponseBuilder.success("Products by category fetched successfully", products);
    }

    @GetMapping("/brand/{brandId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponseDto<ProductResponseDto>> getProductsByBrand(
            @PathVariable Long brandId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<ProductResponseDto> products = productService.getProductsByBrand(brandId, pageable);
        return ApiResponseBuilder.success("Products by brand fetched successfully", products);
    }

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
        description = "Requires ADMIN role"
    )
    @PutMapping("/{productId}/activate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activateProduct(@PathVariable Long productId) {
        productService.activateProduct(productId);
        return ApiResponseBuilder.success("Product activated successfully", null);
    }

    // ========================= DEACTIVATE =========================
    @Operation(
        summary = "Deactivate product",
        description = "Requires ADMIN role"
    )
    @PutMapping("/{productId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivateProduct(@PathVariable Long productId) {
        productService.deactivateProduct(productId);
        return ApiResponseBuilder.success("Product deactivated successfully", null);
    }

    // ========================= DELETE (SOFT DELETE) =========================
    @Operation(
        summary = "Delete product",
        description = "Requires ADMIN role"
    )
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ApiResponseBuilder.success("Product deleted successfully", null);
    }
}
