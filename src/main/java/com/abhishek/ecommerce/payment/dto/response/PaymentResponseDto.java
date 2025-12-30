package com.abhishek.ecommerce.payment.dto.response;

import com.abhishek.ecommerce.payment.entity.PaymentMethod;
import com.abhishek.ecommerce.payment.entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentResponseDto {

    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String transactionId;
}

