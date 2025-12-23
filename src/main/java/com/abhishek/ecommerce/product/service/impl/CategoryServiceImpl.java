package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.product.repository.CategoryRepository;
import com.abhishek.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category update(Long id, Category category) {
        Category existing = getById(id);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setActive(category.getActive());
        return categoryRepository.save(existing);
    }

    public void deactivate(Long id) {
        Category category = getById(id);
        category.setActive(false);
        categoryRepository.save(category);
    }
}

