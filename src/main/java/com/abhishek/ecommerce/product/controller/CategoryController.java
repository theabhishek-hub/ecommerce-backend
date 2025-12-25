package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.product.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ApiResponse<Category> createCategory(@RequestBody Category category) {
        return ApiResponseBuilder.success("Category created successfully", categoryService.createCategory(category));
    }

    @GetMapping
    public ApiResponse<List<Category>> getAllCategories() {
        return ApiResponseBuilder.success("Categories fetched successfully", categoryService.getAllCategories());
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<Category> getCategoryById(@PathVariable Long categoryId) {
        return ApiResponseBuilder.success("Category fetched successfully", categoryService.getCategoryById(categoryId));
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<Category> update(@PathVariable Long categoryId, @RequestBody Category category) {
        return ApiResponseBuilder.success("Category updated successfully", categoryService.updateCategory(categoryId, category));
    }

    @PatchMapping("/{categoryId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivate(@PathVariable Long categoryId) {
        categoryService.deactivateCategory(categoryId);
        return ApiResponseBuilder.success("Category deactivated successfully");
    }

    @PatchMapping("/{categoryId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activate(@PathVariable Long categoryId) {
        categoryService.activateCategory(categoryId);
        return ApiResponseBuilder.success("Category activated successfully");
    }

    @GetMapping("/active")
    public ApiResponse<List<Category>> getAllActiveCategories() {
        return ApiResponseBuilder.success("Active categories fetched successfully", categoryService.getAllActiveCategories());
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponseBuilder.success("Category deleted successfully");
    }

}
