package com.abhishek.ecommerce.order.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id, "ORDER_NOT_FOUND");
    }
}

