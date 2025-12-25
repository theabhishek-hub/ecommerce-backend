package com.abhishek.ecommerce.payment.service;

import com.abhishek.ecommerce.payment.entity.Payment;
import com.abhishek.ecommerce.payment.entity.PaymentMethod;

public interface PaymentService {

    Payment createPayment(Long orderId, PaymentMethod method);

    Payment getPaymentById(Long paymentId);

    Payment getPaymentByOrderId(Long OrderId);

    Payment markPaymentSuccess(Long PaymentId);

    Payment refundPayment(Long paymentId);
}

