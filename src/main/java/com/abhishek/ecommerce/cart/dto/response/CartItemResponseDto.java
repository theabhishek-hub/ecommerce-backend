package com.abhishek.ecommerce.cart.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartItemResponseDto {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal priceAmount;
    private String currency;
    private Integer quantity;
}

