package com.abhishek.ecommerce.product.mapper;

import com.abhishek.ecommerce.product.dto.request.CategoryCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.CategoryUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    // ================= CREATE =================
    public Category toEntity(CategoryCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        return category;
    }

    // ================= RESPONSE =================
    public CategoryResponseDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setStatus(category.getStatus() != null ? category.getStatus().name() : null);

        return dto;
    }

    // ================= UPDATE =================
    public void updateEntityFromDto(CategoryUpdateRequestDto dto, Category category) {
        if (dto == null || category == null) {
            return;
        }

        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
    }
}

