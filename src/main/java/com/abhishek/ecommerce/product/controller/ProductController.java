package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<Product> create(@RequestBody Product product) {
        return ApiResponseBuilder.success("Product created successfully", productService.createProduct(product));
    }

    @GetMapping("/{productId}")
    public ApiResponse<Product> getById(@PathVariable Long productId) {
        return ApiResponseBuilder.success("Product fetched successfully", productService.getProductById(productId));
    }

    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() {
        return ApiResponseBuilder.success("Products fetched successfully", productService.getAllProducts());
    }

    @PutMapping("/{productId}")
    public ApiResponse<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return ApiResponseBuilder.success("Product updated successfully", productService.updateProduct(id, product));
    }

    @PatchMapping("/{productId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ApiResponseBuilder.success("Product deactivated successfully");
    }

    @PatchMapping("/{productId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ApiResponseBuilder.success("Product activated successfully");
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long productId)
    {
        productService.deleteProduct(productId);
        return ApiResponseBuilder.success("Product deleted successfully");
    }

    @GetMapping("/active")
    public ApiResponse<List<Product>> getAllActiveProducts()
    {
        return ApiResponseBuilder.success("Active products fetched successfully", productService.getAllActiveProducts());
    }

}
