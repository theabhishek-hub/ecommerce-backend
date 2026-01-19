package com.abhishek.ecommerce.payment.gateway.razorpay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RazorpayVerifyPaymentRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotBlank(message = "razorpay_order_id is required")
    private String razorpayOrderId;

    @NotBlank(message = "razorpay_payment_id is required")
    private String razorpayPaymentId;

    @NotBlank(message = "razorpay_signature is required")
    private String razorpaySignature;
}

