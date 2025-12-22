package com.abhishek.ecommerce.user.exception;

/**
 * Thrown when a user is not found.
 *
 * Global exception handling will be added later.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
