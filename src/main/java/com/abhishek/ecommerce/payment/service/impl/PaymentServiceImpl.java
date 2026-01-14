package com.abhishek.ecommerce.payment.service.impl;

import com.abhishek.ecommerce.shared.enums.PaymentStatus;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.shared.enums.OrderStatus;
import com.abhishek.ecommerce.order.exception.OrderNotFoundException;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.payment.dto.request.PaymentCreateRequestDto;
import com.abhishek.ecommerce.payment.dto.response.PaymentResponseDto;
import com.abhishek.ecommerce.payment.entity.*;
import com.abhishek.ecommerce.payment.exception.PaymentNotFoundException;
import com.abhishek.ecommerce.payment.mapper.PaymentMapper;
import com.abhishek.ecommerce.payment.repository.PaymentRepository;
import com.abhishek.ecommerce.payment.service.PaymentService;
import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    // ========================= CREATE =========================
    @Override
    public PaymentResponseDto createPayment(PaymentCreateRequestDto requestDto) {
        log.info("createPayment started for orderId={} method={}", requestDto.getOrderId(), requestDto.getPaymentMethod());

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(requestDto.getOrderId()));

        // Check ownership: User can only create payment for their own order, or admin
        String currentUsername = securityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        // Admin can create payment for any order, regular users only for their own
        if (!currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (order.getUser() == null || !order.getUser().getId().equals(currentUserId)) {
                log.warn("Access denied: User {} attempted to create payment for order {} owned by user {}",
                        currentUserId, requestDto.getOrderId(), order.getUser() != null ? order.getUser().getId() : "null");
                throw new AccessDeniedException("You do not have permission to create payment for this order");
            }
        }

        paymentRepository.findByOrderId(requestDto.getOrderId()).ifPresent(p -> {
            log.warn("createPayment duplicate payment orderId={}", requestDto.getOrderId());
            throw new RuntimeException("Payment already exists for this order");
        });

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(requestDto.getPaymentMethod());
        payment.setAmount(order.getTotalAmount());

        if (requestDto.getPaymentMethod() == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(null);
            log.info("createPayment COD orderId={}", requestDto.getOrderId());
        } else {
            // ONLINE (mock)
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId("TXN-" + System.currentTimeMillis());
            log.info("createPayment ONLINE orderId={} txnId={}", requestDto.getOrderId(), payment.getTransactionId());
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("createPayment completed paymentId={} orderId={}", savedPayment.getId(), requestDto.getOrderId());
        return paymentMapper.toDto(savedPayment);
    }

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Ownership check is handled by @PreAuthorize in controller, but add service-level check as defense
        validatePaymentAccess(payment);
        
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));

        // Ownership check is handled by @PreAuthorize in controller, but add service-level check as defense
        validatePaymentAccess(payment);
        
        return paymentMapper.toDto(payment);
    }

    private void validatePaymentAccess(Payment payment) {
        String currentUsername = securityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        // Admin can access any payment
        if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
            return;
        }

        // Regular users can only access their own payments
        if (payment.getOrder() == null || payment.getOrder().getUser() == null) {
            throw new AccessDeniedException("Payment is not associated with a valid order");
        }

        Long currentUserId = securityUtils.getCurrentUserId();
        if (!payment.getOrder().getUser().getId().equals(currentUserId)) {
            log.warn("Access denied: User {} attempted to access payment {} for order {} owned by user {}",
                    currentUserId, payment.getId(), payment.getOrder().getId(),
                    payment.getOrder().getUser().getId());
            throw new AccessDeniedException("You do not have permission to access this payment");
        }
    }

    // ========================= MARK SUCCESS =========================
    @Override
    public PaymentResponseDto markPaymentSuccess(Long paymentId) {
        log.info("markPaymentSuccess started for paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Prevent duplicate success
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("markPaymentSuccess already SUCCESS paymentId={}", paymentId);
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
            log.error("markPaymentSuccess no order linked paymentId={}", paymentId);
            throw new RuntimeException("Order not linked with payment");
        }

        // Update order status
        order.setStatus(OrderStatus.PAID);

        // Save order FIRST (important)
        orderRepository.save(order);

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        log.info("markPaymentSuccess completed paymentId={} orderId={}", paymentId, order.getId());
        return paymentMapper.toDto(savedPayment);
    }

    // ========================= REFUND =========================
    @Override
    public PaymentResponseDto refundPayment(Long paymentId) {
        log.info("refundPayment started for paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            log.warn("refundPayment invalid status paymentId={} status={}", paymentId, payment.getStatus());
            throw new IllegalStateException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);
        log.info("refundPayment completed paymentId={}", paymentId);
        return paymentMapper.toDto(savedPayment);
    }



}

