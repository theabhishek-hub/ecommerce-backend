package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.product.dto.request.CategoryCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.CategoryUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ========================= CREATE =========================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponseDto> createCategory(
            @Valid @RequestBody CategoryCreateRequestDto requestDto
    ) {
        CategoryResponseDto response = categoryService.createCategory(requestDto);
        return ApiResponseBuilder.created("Category created successfully", response);
    }

    // ========================= UPDATE =========================
    @PutMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CategoryResponseDto> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequestDto requestDto
    ) {
        CategoryResponseDto response = categoryService.updateCategory(categoryId, requestDto);
        return ApiResponseBuilder.success("Category updated successfully", response);
    }

    // ========================= GET BY ID =========================
    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CategoryResponseDto> getCategoryById(@PathVariable Long categoryId) {
        CategoryResponseDto response = categoryService.getCategoryById(categoryId);
        return ApiResponseBuilder.success("Category fetched successfully", response);
    }

    // ========================= GET ALL =========================
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ApiResponseBuilder.success("Categories fetched successfully", categories);
    }

    // ========================= GET ALL ACTIVE =========================
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<CategoryResponseDto>> getAllActiveCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllActiveCategories();
        return ApiResponseBuilder.success("Active categories fetched successfully", categories);
    }

    // ========================= ACTIVATE =========================
    @PutMapping("/{categoryId}/activate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> activateCategory(@PathVariable Long categoryId) {
        categoryService.activateCategory(categoryId);
        return ApiResponseBuilder.success("Category activated successfully", null);
    }

    // ========================= DEACTIVATE =========================
    @PutMapping("/{categoryId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deactivateCategory(@PathVariable Long categoryId) {
        categoryService.deactivateCategory(categoryId);
        return ApiResponseBuilder.success("Category deactivated successfully", null);
    }

    // ========================= DELETE (SOFT DELETE) =========================
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponseBuilder.success("Category deleted successfully", null);
    }
}
