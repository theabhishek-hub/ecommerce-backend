package com.abhishek.ecommerce.order.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto {

    private Long id;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private List<OrderItemResponseDto> items;
}

