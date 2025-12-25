package com.abhishek.ecommerce.payment.service.impl;

import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.entity.OrderStatus;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.payment.entity.*;
import com.abhishek.ecommerce.payment.repository.PaymentRepository;
import com.abhishek.ecommerce.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Payment initiatePayment(Long orderId, PaymentMethod method) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        paymentRepository.findByOrderId(orderId).ifPresent(p -> {
            throw new RuntimeException("Payment already exists for this order");
        });

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(method);
        payment.setAmount(order.getTotalAmount());

        if (method == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(null);
        } else {
            // ONLINE (mock)
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId("TXN-" + System.currentTimeMillis());
        }

        return paymentRepository.save(payment);
    }
    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order"));
    }

    @Override
    @Transactional
    public Payment markPaymentSuccess(Long paymentId) {

        // 1. Fetch payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // 2. Prevent duplicate success
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }

        // 3. Mark payment as SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);

        // COD → transactionId is allowed to be NULL
        // Online → later you will set gateway transaction id
        payment.setTransactionId(
                payment.getTransactionId() == null ? null : payment.getTransactionId()
        );

        // 4. Fetch related order
        Order order = payment.getOrder();
        if (order == null) {
            throw new RuntimeException("Order not linked with payment");
        }

        // 5. Update order status
        order.setStatus(OrderStatus.PAID);

        // 6. Save order FIRST (important)
        orderRepository.save(order);

        // 7. Save payment
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }



}

