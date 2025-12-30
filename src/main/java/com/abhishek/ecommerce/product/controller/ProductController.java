package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ========================= CREATE =========================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductCreateRequestDto requestDto
    ) {
        ProductResponseDto response = productService.createProduct(requestDto);
        return ApiResponseBuilder.created("Product created successfully", response);
    }

    // ========================= UPDATE =========================
    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
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
        return ApiResponseBuilder.success("Products fetched successfully", products);
    }

    // ========================= GET ALL ACTIVE =========================
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ProductResponseDto>> getAllActiveProducts() {
        List<ProductResponseDto> products = productService.getAllActiveProducts();
        return ApiResponseBuilder.success("Active products fetched successfully", products);
    }

    // ========================= ACTIVATE =========================
    @PutMapping("/{productId}/activate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> activateProduct(@PathVariable Long productId) {
        productService.activateProduct(productId);
        return ApiResponseBuilder.success("Product activated successfully", null);
    }

    // ========================= DEACTIVATE =========================
    @PutMapping("/{productId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deactivateProduct(@PathVariable Long productId) {
        productService.deactivateProduct(productId);
        return ApiResponseBuilder.success("Product deactivated successfully", null);
    }

    // ========================= DELETE (SOFT DELETE) =========================
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ApiResponseBuilder.success("Product deleted successfully", null);
    }
}
