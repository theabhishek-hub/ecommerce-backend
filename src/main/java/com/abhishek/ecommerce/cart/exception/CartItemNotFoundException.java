package com.abhishek.ecommerce.cart.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class CartItemNotFoundException extends BusinessException {

    public CartItemNotFoundException(Long id) {
        super("Cart item not found with id: " + id, "CART_ITEM_NOT_FOUND");
    }
}

