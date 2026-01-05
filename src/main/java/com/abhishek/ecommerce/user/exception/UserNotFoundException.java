package com.abhishek.ecommerce.user.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id, "USER_NOT_FOUND");
    }
}

