package com.abhishek.ecommerce.product.mapper;

import com.abhishek.ecommerce.product.dto.request.CategoryCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.CategoryUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // ================= CREATE =================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Category toEntity(CategoryCreateRequestDto dto);

    // ================= RESPONSE =================
    @Mapping(target = "status", expression = "java(category.getStatus() != null ? category.getStatus().name() : null)")
    CategoryResponseDto toDto(Category category);

    // ================= UPDATE =================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromDto(CategoryUpdateRequestDto dto, @MappingTarget Category category);
}

