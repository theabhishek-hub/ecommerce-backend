package com.abhishek.ecommerce.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Add product to cart request payload")
public class AddToCartRequestDto {

    @Schema(description = "Product ID to add to cart", example = "123", required = true)
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Schema(description = "Quantity of the product to add", example = "2", required = true)
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}

