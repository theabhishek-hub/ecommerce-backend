package com.abhishek.ecommerce.inventory.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryResponseDto {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
}

