package com.abhishek.ecommerce.product.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id, "PRODUCT_NOT_FOUND");
    }
}

