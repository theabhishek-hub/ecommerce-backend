package com.abhishek.ecommerce.product.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponseDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal priceAmount;
    private String currency;
    private String sku;
    private String imageUrl;
    private String status;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
}

