package com.abhishek.ecommerce.auth.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Invalid email or password", "INVALID_CREDENTIALS");
    }
}