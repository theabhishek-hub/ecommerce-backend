package com.abhishek.ecommerce.product.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class ProductAlreadyExistsException extends BusinessException {

    public ProductAlreadyExistsException(String identifier) {
        super("Product already exists with identifier: " + identifier, "PRODUCT_ALREADY_EXISTS");
    }
}

