package com.abhishek.ecommerce.product.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class CategoryAlreadyExistsException extends BusinessException {

    public CategoryAlreadyExistsException(String identifier) {
        super("Category already exists with identifier: " + identifier, "CATEGORY_ALREADY_EXISTS");
    }
}

