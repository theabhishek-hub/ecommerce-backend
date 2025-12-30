package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.dto.request.CategoryCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.CategoryUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.product.entity.CategoryStatus;
import com.abhishek.ecommerce.product.exception.CategoryAlreadyExistsException;
import com.abhishek.ecommerce.product.exception.CategoryNotFoundException;
import com.abhishek.ecommerce.product.mapper.CategoryMapper;
import com.abhishek.ecommerce.product.repository.CategoryRepository;
import com.abhishek.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ========================= CREATE =========================
    @Override
    public CategoryResponseDto createCategory(CategoryCreateRequestDto requestDto) {

        // Check duplicate name
        if (categoryRepository.existsByName(requestDto.getName())) {
            throw new CategoryAlreadyExistsException(
                    "Category already exists with name: " + requestDto.getName()
            );
        }

        Category category = categoryMapper.toEntity(requestDto);
        category.setStatus(CategoryStatus.ACTIVE);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    // ========================= UPDATE =========================
    @Override
    public CategoryResponseDto updateCategory(Long categoryId, CategoryUpdateRequestDto requestDto) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        // Update only provided fields
        if (requestDto.getName() != null) {
            // Check if name is already taken by another category
            categoryRepository.findByName(requestDto.getName())
                    .ifPresent(existingCategory -> {
                        if (!existingCategory.getId().equals(categoryId)) {
                            throw new CategoryAlreadyExistsException("Category already exists with name: " + requestDto.getName());
                        }
                    });
            category.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            category.setDescription(requestDto.getDescription());
        }

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(updatedCategory);
    }

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllActiveCategories() {
        return categoryRepository.findAllByStatus(CategoryStatus.ACTIVE)
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========================= STATUS =========================
    @Override
    @Transactional
    public void activateCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        category.setStatus(CategoryStatus.ACTIVE);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deactivateCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
    }

    // ========================= DELETE (SOFT) =========================
    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
    }
}

