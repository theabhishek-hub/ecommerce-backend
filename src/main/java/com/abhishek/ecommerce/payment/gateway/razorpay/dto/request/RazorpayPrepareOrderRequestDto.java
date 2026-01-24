package com.abhishek.ecommerce.payment.gateway.razorpay.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for preparing Razorpay payment without creating a database order
 * Used when user selects ONLINE payment but payment hasn't been completed yet
 */
@Getter
@Setter
public class RazorpayPrepareOrderRequestDto {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount;  // Amount in paise (e.g., 10000 for ₹100)

    @NotNull(message = "Currency is required")
    private String currency;  // e.g., "INR"
}
