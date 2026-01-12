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

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setItems(List<OrderItemResponseDto> items) {
        this.items = items;
    }
}

