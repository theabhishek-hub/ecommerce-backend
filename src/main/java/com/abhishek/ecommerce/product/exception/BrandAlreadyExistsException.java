package com.abhishek.ecommerce.product.exception;

public class BrandAlreadyExistsException extends RuntimeException {

    public BrandAlreadyExistsException(String message) {
        super(message);
    }
}

