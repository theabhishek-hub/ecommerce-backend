package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.dto.request.CategoryCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.CategoryUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.shared.enums.CategoryStatus;
import com.abhishek.ecommerce.product.exception.CategoryAlreadyExistsException;
import com.abhishek.ecommerce.product.exception.CategoryNotFoundException;
import com.abhishek.ecommerce.product.mapper.CategoryMapper;
import com.abhishek.ecommerce.product.repository.CategoryRepository;
import com.abhishek.ecommerce.product.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryResponseDto categoryResponseDto;
    private CategoryCreateRequestDto createRequestDto;
    private CategoryUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic devices and gadgets");
        category.setStatus(CategoryStatus.ACTIVE);

        categoryResponseDto = new CategoryResponseDto();
        categoryResponseDto.setId(1L);
        categoryResponseDto.setName("Electronics");
        categoryResponseDto.setDescription("Electronic devices and gadgets");
        categoryResponseDto.setStatus("ACTIVE");

        createRequestDto = new CategoryCreateRequestDto();
        createRequestDto.setName("Electronics");
        createRequestDto.setDescription("Electronic devices and gadgets");

        updateRequestDto = new CategoryUpdateRequestDto();
        updateRequestDto.setName("Updated Electronics");
        updateRequestDto.setDescription("Updated electronic devices and gadgets");
    }

    @Test
    void createCategory_ShouldCreateCategorySuccessfully() {
        // Given
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryMapper.toEntity(any(CategoryCreateRequestDto.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryResponseDto);

        // When
        CategoryResponseDto result = categoryService.createCategory(createRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowException_WhenCategoryAlreadyExists() {
        // Given
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequestDto))
                .isInstanceOf(CategoryAlreadyExistsException.class);
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryResponseDto);

        // When
        CategoryResponseDto result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_ShouldThrowException_WhenCategoryNotFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(1L))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        List<Category> categories = List.of(category);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryResponseDto);

        // When
        List<CategoryResponseDto> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(1);
        verify(categoryRepository).findAll();
    }

    @Test
    void getAllActiveCategories_ShouldReturnActiveCategories() {
        // Given
        List<Category> categories = List.of(category);
        when(categoryRepository.findAllByStatus(CategoryStatus.ACTIVE)).thenReturn(categories);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(categoryResponseDto);

        // When
        List<CategoryResponseDto> result = categoryService.getAllActiveCategories();

        // Then
        assertThat(result).hasSize(1);
        verify(categoryRepository).findAllByStatus(CategoryStatus.ACTIVE);
    }

    @Test
    void updateCategory_ShouldUpdateCategorySuccessfully() {
        // Given
        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Updated Electronics");
        updatedCategory.setDescription("Updated electronic devices and gadgets");
        updatedCategory.setStatus(CategoryStatus.ACTIVE);

        CategoryResponseDto updatedResponseDto = new CategoryResponseDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Updated Electronics");
        updatedResponseDto.setDescription("Updated electronic devices and gadgets");
        updatedResponseDto.setStatus("ACTIVE");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(updatedResponseDto);

        // When
        CategoryResponseDto result = categoryService.updateCategory(1L, updateRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Electronics");
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void activateCategory_ShouldActivateCategory() {
        // Given
        Category inactiveCategory = new Category();
        inactiveCategory.setId(1L);
        inactiveCategory.setName("Electronics");
        inactiveCategory.setStatus(CategoryStatus.INACTIVE);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(inactiveCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // When
        categoryService.activateCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deactivateCategory_ShouldDeactivateCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // When
        categoryService.deactivateCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldDeleteCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }
}