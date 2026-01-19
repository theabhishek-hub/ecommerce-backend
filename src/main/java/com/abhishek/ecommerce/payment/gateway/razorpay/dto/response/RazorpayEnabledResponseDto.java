package com.abhishek.ecommerce.payment.gateway.razorpay.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RazorpayEnabledResponseDto {
    private boolean enabled;
    private String keyId; // safe to expose; secret is never returned
}

