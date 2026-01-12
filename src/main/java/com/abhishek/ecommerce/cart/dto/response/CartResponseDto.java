package com.abhishek.ecommerce.cart.dto.response;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@Schema(description = "Shopping cart response")
public class CartResponseDto {

    @Schema(description = "Cart ID", example = "456")
    private Long id;

    @Schema(description = "User ID", example = "123")
    private Long userId;

    @Schema(description = "List of cart items")
    private List<CartItemResponseDto> items;
    
}

