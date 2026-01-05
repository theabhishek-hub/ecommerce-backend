package com.abhishek.ecommerce.product.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class CategoryNotFoundException extends BusinessException {

    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id, "CATEGORY_NOT_FOUND");
    }
}

