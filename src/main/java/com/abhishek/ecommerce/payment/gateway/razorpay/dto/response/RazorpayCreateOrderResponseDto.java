package com.abhishek.ecommerce.payment.gateway.razorpay.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RazorpayCreateOrderResponseDto {
    private boolean enabled;
    private String keyId;           // public key used by Razorpay Checkout
    private String razorpayOrderId; // Razorpay order id
    private Long amount;            // amount in paise
    private String currency;        // e.g., INR
    private Long internalOrderId;   // internal order id (for convenience)
}

