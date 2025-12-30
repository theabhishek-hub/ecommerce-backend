package com.abhishek.ecommerce.product.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandUpdateRequestDto {

    @Size(max = 150, message = "Brand name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
}

