package com.abhishek.ecommerce.payment.mapper;

import com.abhishek.ecommerce.payment.dto.response.PaymentResponseDto;
import com.abhishek.ecommerce.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    // ================= RESPONSE =================
    public PaymentResponseDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setAmount(payment.getAmount() != null ? payment.getAmount().getAmount() : null);
        dto.setCurrency(payment.getAmount() != null ? payment.getAmount().getCurrency() : null);
        dto.setTransactionId(payment.getTransactionId());

        return dto;
    }
}

