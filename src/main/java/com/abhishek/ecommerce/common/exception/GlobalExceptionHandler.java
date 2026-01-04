package com.abhishek.ecommerce.common.exception;

import com.abhishek.ecommerce.cart.exception.CartItemNotFoundException;
import com.abhishek.ecommerce.cart.exception.CartNotFoundException;
import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.inventory.exception.InsufficientStockException;
import com.abhishek.ecommerce.inventory.exception.InventoryNotFoundException;
import com.abhishek.ecommerce.order.exception.OrderNotFoundException;
import com.abhishek.ecommerce.payment.exception.PaymentNotFoundException;
import com.abhishek.ecommerce.product.exception.*;
import com.abhishek.ecommerce.user.exception.UserAlreadyExistsException;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        ApiResponseBuilder.failed(
                                HttpStatus.FORBIDDEN,
                                "Access denied"
                        )
                );
    }



    // ========================= USER EXCEPTIONS =========================
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.error("UserNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.error("UserAlreadyExistsException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                ));
    }

    // ========================= PRODUCT EXCEPTIONS =========================
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFound(ProductNotFoundException ex) {
        log.error("ProductNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductAlreadyExists(ProductAlreadyExistsException ex) {
        log.error("ProductAlreadyExistsException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                ));
    }

    // ========================= CATEGORY EXCEPTIONS =========================
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFound(CategoryNotFoundException ex) {
        log.error("CategoryNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryAlreadyExists(CategoryAlreadyExistsException ex) {
        log.error("CategoryAlreadyExistsException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                ));
    }

    // ========================= BRAND EXCEPTIONS =========================
    @ExceptionHandler(BrandNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBrandNotFound(BrandNotFoundException ex) {
        log.error("BrandNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(BrandAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBrandAlreadyExists(BrandAlreadyExistsException ex) {
        log.error("BrandAlreadyExistsException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                ));
    }

    // ========================= CART EXCEPTIONS =========================
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCartNotFound(CartNotFoundException ex) {
        log.error("CartNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCartItemNotFound(CartItemNotFoundException ex) {
        log.error("CartItemNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    // ========================= ORDER EXCEPTIONS =========================
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(OrderNotFoundException ex) {
        log.error("OrderNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    // ========================= PAYMENT EXCEPTIONS =========================
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.error("PaymentNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    // ========================= INVENTORY EXCEPTIONS =========================
    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInventoryNotFound(InventoryNotFoundException ex) {
        log.error("InventoryNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        log.error("InsufficientStockException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()
                ));
    }

    // ========================= VALIDATION EXCEPTIONS =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(ApiResponseBuilder.validationFailed(
                        "Validation failed",
                        errors
                ));
    }

    // ========================= ILLEGAL STATE EXCEPTIONS =========================
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        log.error("IllegalStateException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()
                ));
    }

    // ========================= GENERIC EXCEPTIONS =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseBuilder.failed(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage()
                ));
    }
}
