package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.entity.Category;

import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    Category getCategoryById(Long categoryId);
    List<Category> getAllCategories();
    Category updateCategory(Long categoryId, Category category);
    List<Category> getAllActiveCategories();

    void deactivateCategory(Long categoryId);

    void activateCategory(Long categoryId);

    void deleteCategory(Long categoryId);
}

