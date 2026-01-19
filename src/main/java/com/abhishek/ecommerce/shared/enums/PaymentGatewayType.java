package com.abhishek.ecommerce.shared.enums;

/**
 * Payment gateway type (separate from PaymentMethod).
 *
 * PaymentMethod represents the user-facing choice (e.g., COD vs ONLINE),
 * while PaymentGatewayType represents which processor is used for ONLINE payments.
 */
public enum PaymentGatewayType {
    COD,
    RAZORPAY
}

