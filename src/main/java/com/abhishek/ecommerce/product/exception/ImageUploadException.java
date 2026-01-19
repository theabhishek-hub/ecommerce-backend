package com.abhishek.ecommerce.product.exception;

/**
 * Exception thrown when image upload fails
 */
public class ImageUploadException extends RuntimeException {

    private final String errorCode;

    public ImageUploadException(String message) {
        super(message);
        this.errorCode = "IMAGE_UPLOAD_ERROR";
    }

    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "IMAGE_UPLOAD_ERROR";
    }

    public ImageUploadException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
