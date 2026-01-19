package com.abhishek.ecommerce.payment.gateway.razorpay.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RazorpayCreateOrderRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;
}

