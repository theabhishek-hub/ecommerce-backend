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
    
    // SEARCH
    List<CategoryResponseDto> searchCategoriesByName(String name);
    
    // FILTER
    List<CategoryResponseDto> filterByStatus(String status);
    
    // SORT
    List<CategoryResponseDto> getAllCategoriesSorted(String sortBy, String order);
    
    // COMBINED SEARCH & FILTER & SORT
    List<CategoryResponseDto> searchFilterSort(String name, String status, String sortBy, String order);
    
    // UPDATE
    CategoryResponseDto updateCategory(Long categoryId, CategoryUpdateRequestDto requestDto);
    
    // STATUS OPERATIONS
    void activateCategory(Long categoryId);
    void deactivateCategory(Long categoryId);
    
    // DELETE (soft delete)
    void deleteCategory(Long categoryId);
}

