package com.abhishek.ecommerce.product.mapper;

import com.abhishek.ecommerce.product.dto.request.BrandCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.BrandUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.product.entity.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {

    // ================= CREATE =================
    public Brand toEntity(BrandCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Brand brand = new Brand();
        brand.setName(dto.getName());
        brand.setDescription(dto.getDescription());
        brand.setCountry(dto.getCountry());

        return brand;
    }

    // ================= RESPONSE =================
    public BrandResponseDto toDto(Brand brand) {
        if (brand == null) {
            return null;
        }

        BrandResponseDto dto = new BrandResponseDto();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setDescription(brand.getDescription());
        dto.setCountry(brand.getCountry());
        dto.setStatus(brand.getStatus() != null ? brand.getStatus().name() : null);

        return dto;
    }

    // ================= UPDATE =================
    public void updateEntityFromDto(BrandUpdateRequestDto dto, Brand brand) {
        if (dto == null || brand == null) {
            return;
        }

        if (dto.getName() != null) {
            brand.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            brand.setDescription(dto.getDescription());
        }
        if (dto.getCountry() != null) {
            brand.setCountry(dto.getCountry());
        }
    }
}

