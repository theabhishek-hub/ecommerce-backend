package com.abhishek.ecommerce.product.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class BrandAlreadyExistsException extends BusinessException {

    public BrandAlreadyExistsException(String identifier) {
        super("Brand already exists with identifier: " + identifier, "BRAND_ALREADY_EXISTS");
    }
}

