package com.abhishek.ecommerce.payment.service.impl;

import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.entity.OrderStatus;
import com.abhishek.ecommerce.order.exception.OrderNotFoundException;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.payment.dto.request.PaymentCreateRequestDto;
import com.abhishek.ecommerce.payment.dto.response.PaymentResponseDto;
import com.abhishek.ecommerce.payment.entity.*;
import com.abhishek.ecommerce.payment.exception.PaymentNotFoundException;
import com.abhishek.ecommerce.payment.mapper.PaymentMapper;
import com.abhishek.ecommerce.payment.repository.PaymentRepository;
import com.abhishek.ecommerce.payment.service.PaymentService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    // ========================= CREATE =========================
    @Override
    public PaymentResponseDto createPayment(PaymentCreateRequestDto requestDto) {

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + requestDto.getOrderId()));

        paymentRepository.findByOrderId(requestDto.getOrderId()).ifPresent(p -> {
            throw new RuntimeException("Payment already exists for this order");
        });

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(requestDto.getPaymentMethod());
        payment.setAmount(order.getTotalAmount());

        if (requestDto.getPaymentMethod() == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(null);
        } else {
            // ONLINE (mock)
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId("TXN-" + System.currentTimeMillis());
        }

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order id: " + orderId));
        return paymentMapper.toDto(payment);
    }

    // ========================= MARK SUCCESS =========================
    @Override
    public PaymentResponseDto markPaymentSuccess(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        // Prevent duplicate success
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return paymentMapper.toDto(payment);
        }

        // Mark payment as SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);

        // COD → transactionId is allowed to be NULL
        // Online → later you will set gateway transaction id
        payment.setTransactionId(
                payment.getTransactionId() == null ? null : payment.getTransactionId()
        );

        // Fetch related order
        Order order = payment.getOrder();
        if (order == null) {
            throw new RuntimeException("Order not linked with payment");
        }

        // Update order status
        order.setStatus(OrderStatus.PAID);

        // Save order FIRST (important)
        orderRepository.save(order);

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    // ========================= REFUND =========================
    @Override
    public PaymentResponseDto refundPayment(Long paymentId) {

        Payment payment = paymentRepository.findByOrderId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }



}

