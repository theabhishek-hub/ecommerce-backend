package com.abhishek.ecommerce.product.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandResponseDto {

    private Long id;
    private String name;
    private String description;
    private String country;
    private String status;
}

