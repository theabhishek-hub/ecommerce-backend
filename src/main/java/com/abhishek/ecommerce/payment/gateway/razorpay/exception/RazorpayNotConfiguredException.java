package com.abhishek.ecommerce.payment.gateway.razorpay.exception;

public class RazorpayNotConfiguredException extends RuntimeException {
    public RazorpayNotConfiguredException() {
        super("Razorpay is not configured. Please set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET environment variables.");
    }
}

