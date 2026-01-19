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

    @Schema(description = "Product SKU", example = "IPHONE-15-128GB")
    private String sku;

    @Schema(description = "Seller ID (null for admin products)", example = "10")
    private Long sellerId;

    @Schema(description = "Seller name (null for admin products)", example = "John's Electronics")
    private String sellerName;

    @Schema(description = "Available quantity in stock", example = "50")
    private Integer quantity;

    public void setId(Long id) {
        this.id = id;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

