package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.product.entity.CategoryStatus;
import com.abhishek.ecommerce.product.repository.CategoryRepository;
import com.abhishek.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category updateCategory(Long categoryId, Category category) {
        Category existing = getCategoryById(categoryId);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        return categoryRepository.save(existing);
    }

    @Override
    @Transactional
    public void deactivateCategory(Long categoryId) {
        Category category = getCategory(categoryId);
        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void activateCategory(Long categoryId) {
        Category category = getCategory(categoryId);
        category.setStatus(CategoryStatus.ACTIVE);
        categoryRepository.save(category);
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllByStatus(CategoryStatus.ACTIVE);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
    }

}

