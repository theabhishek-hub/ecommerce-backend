package com.abhishek.ecommerce.payment.service;

import com.abhishek.ecommerce.payment.dto.request.PaymentCreateRequestDto;
import com.abhishek.ecommerce.payment.dto.response.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto createPayment(PaymentCreateRequestDto requestDto);

    PaymentResponseDto getPaymentById(Long paymentId);

    PaymentResponseDto getPaymentByOrderId(Long orderId);

    PaymentResponseDto markPaymentSuccess(Long paymentId);

    PaymentResponseDto refundPayment(Long paymentId);
}

