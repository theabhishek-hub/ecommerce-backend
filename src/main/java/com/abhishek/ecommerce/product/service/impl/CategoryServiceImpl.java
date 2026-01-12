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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ========================= CREATE =========================
    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDto createCategory(CategoryCreateRequestDto requestDto) {
        log.info("createCategory started for name={}", requestDto.getName());

        // Check duplicate name
        if (categoryRepository.existsByName(requestDto.getName())) {
            log.warn("createCategory duplicate name={}", requestDto.getName());
            throw new CategoryAlreadyExistsException(requestDto.getName());
        }

        Category category = categoryMapper.toEntity(requestDto);
        category.setStatus(CategoryStatus.ACTIVE);

        Category savedCategory = categoryRepository.save(category);
        log.info("createCategory completed categoryId={} name={}", savedCategory.getId(), requestDto.getName());
        return categoryMapper.toDto(savedCategory);
    }

    // ========================= UPDATE =========================
    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDto updateCategory(Long categoryId, CategoryUpdateRequestDto requestDto) {
        log.info("updateCategory started for categoryId={}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // Update only provided fields
        if (requestDto.getName() != null) {
            // Check if name is already taken by another category
            categoryRepository.findByName(requestDto.getName())
                    .ifPresent(existingCategory -> {
                        if (!existingCategory.getId().equals(categoryId)) {
                            log.warn("updateCategory duplicate name={} categoryId={}", requestDto.getName(), categoryId);
                            throw new CategoryAlreadyExistsException(requestDto.getName());
                        }
                    });
            category.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            category.setDescription(requestDto.getDescription());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("updateCategory completed categoryId={}", categoryId);
        return categoryMapper.toDto(updatedCategory);
    }

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#categoryId")
    public CategoryResponseDto getCategoryById(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

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
    @Cacheable(value = "categories", key = "'active'")
    public List<CategoryResponseDto> getAllActiveCategories() {
        return categoryRepository.findAllByStatus(CategoryStatus.ACTIVE)
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========================= STATUS =========================
    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void activateCategory(Long categoryId) {
        log.info("activateCategory started for categoryId={}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        category.setStatus(CategoryStatus.ACTIVE);
        categoryRepository.save(category);
        log.info("activateCategory completed categoryId={}", categoryId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deactivateCategory(Long categoryId) {
        log.info("deactivateCategory started for categoryId={}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
        log.info("deactivateCategory completed categoryId={}", categoryId);
    }

    // ========================= DELETE (SOFT) =========================
    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long categoryId) {
        log.info("deleteCategory started for categoryId={}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
        log.info("deleteCategory completed categoryId={}", categoryId);
    }
}
