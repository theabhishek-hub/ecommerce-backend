package com.abhishek.ecommerce.inventory.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(Long productId, int requested, int available) {
        super("Insufficient stock for product " + productId + ": requested " + requested + ", available " + available, "INSUFFICIENT_STOCK");
    }
}

