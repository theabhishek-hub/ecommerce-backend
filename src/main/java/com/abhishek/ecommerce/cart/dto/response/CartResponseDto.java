package com.abhishek.ecommerce.cart.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartResponseDto {

    private Long id;
    private Long userId;
    private List<CartItemResponseDto> items;
}

