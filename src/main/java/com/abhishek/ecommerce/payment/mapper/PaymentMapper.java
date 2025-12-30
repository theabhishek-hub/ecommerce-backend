package com.abhishek.ecommerce.payment.mapper;

import com.abhishek.ecommerce.payment.dto.response.PaymentResponseDto;
import com.abhishek.ecommerce.payment.entity.Payment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    // ================= RESPONSE =================
    @Mapping(target = "orderId", expression = "java(payment.getOrder() != null ? payment.getOrder().getId() : null)")
    @Mapping(target = "amount", expression = "java(payment.getAmount() != null ? payment.getAmount().getAmount() : null)")
    @Mapping(target = "currency", expression = "java(payment.getAmount() != null ? payment.getAmount().getCurrency() : null)")
    PaymentResponseDto toDto(Payment payment);
}

