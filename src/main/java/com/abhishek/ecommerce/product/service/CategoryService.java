package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.entity.Category;

import java.util.List;

public interface CategoryService {
    Category create(Category category);
    Category getById(Long id);
    List<Category> getAll();
    Category update(Long id, Category category);
    void deactivate(Long id);
}

