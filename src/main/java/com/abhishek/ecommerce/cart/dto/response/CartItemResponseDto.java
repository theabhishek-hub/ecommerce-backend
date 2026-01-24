package com.abhishek.ecommerce.cart.dto.response;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Cart item details")
public class CartItemResponseDto {

    @Schema(description = "Cart item ID", example = "789")
    private Long id;

    @Schema(description = "Product ID", example = "123")
    private Long productId;

    @Schema(description = "Product name", example = "iPhone 15")
    private String productName;

    @Schema(description = "Product price amount", example = "999.99")
    private BigDecimal priceAmount;

    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Schema(description = "Quantity in cart", example = "2")
    private Integer quantity;
    
    @Schema(description = "Product image URL")
    private String imageUrl;
    
}