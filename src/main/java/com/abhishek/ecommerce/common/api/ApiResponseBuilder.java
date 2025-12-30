package com.abhishek.ecommerce.common.api;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


public final class ApiResponseBuilder {

    private ApiResponseBuilder() {
        // prevent instantiation
    }

    /* ===================== SUCCESS RESPONSES ===================== */

    // 200 OK (GET, UPDATE, ACTIVATE, DEACTIVATE, etc.)
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 201 CREATED (POST / create)
    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // 204 NO CONTENT (DELETE)
    public static ApiResponse<Void> noContent(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.NO_CONTENT.value())
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /* ===================== ERROR RESPONSES ===================== */

    // Generic error with status
    public static ApiResponse<Void> failed(HttpStatus status, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .status(status.value())
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    // Validation errors (Map<String, String>)
    public static <T> ApiResponse<T> validationFailed(String message, T errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .data(errors)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
