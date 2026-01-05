package com.abhishek.ecommerce.cart.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class CartNotFoundException extends BusinessException {

    public CartNotFoundException(Long id) {
        super("Cart not found with id: " + id, "CART_NOT_FOUND");
    }
}

