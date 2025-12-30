package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.dto.request.CategoryCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.CategoryUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    
    // CREATE
    CategoryResponseDto createCategory(CategoryCreateRequestDto requestDto);
    
    // READ
    CategoryResponseDto getCategoryById(Long categoryId);
    List<CategoryResponseDto> getAllCategories();
    List<CategoryResponseDto> getAllActiveCategories();
    
    // UPDATE
    CategoryResponseDto updateCategory(Long categoryId, CategoryUpdateRequestDto requestDto);
    
    // STATUS OPERATIONS
    void activateCategory(Long categoryId);
    void deactivateCategory(Long categoryId);
    
    // DELETE (soft delete)
    void deleteCategory(Long categoryId);
}

