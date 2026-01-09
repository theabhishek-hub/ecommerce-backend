package com.abhishek.ecommerce.inventory.dto.response;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Product inventory information")
public class InventoryResponseDto {

    @Schema(description = "Inventory record ID", example = "456")
    private Long id;

    @Schema(description = "Product ID", example = "123")
    private Long productId;

    @Schema(description = "Product name", example = "iPhone 15")
    private String productName;

    @Schema(description = "Available quantity in stock", example = "50")
    private Integer quantity;
}

