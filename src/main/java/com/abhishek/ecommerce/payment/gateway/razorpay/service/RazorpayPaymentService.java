package com.abhishek.ecommerce.payment.gateway.razorpay.service;

import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.exception.OrderNotFoundException;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.payment.entity.Payment;
import com.abhishek.ecommerce.payment.entity.PaymentMethod;
import com.abhishek.ecommerce.payment.gateway.razorpay.RazorpayProperties;
import com.abhishek.ecommerce.payment.gateway.razorpay.dto.request.RazorpayCreateOrderRequestDto;
import com.abhishek.ecommerce.payment.gateway.razorpay.dto.request.RazorpayVerifyPaymentRequestDto;
import com.abhishek.ecommerce.payment.gateway.razorpay.dto.response.RazorpayCreateOrderResponseDto;
import com.abhishek.ecommerce.payment.gateway.razorpay.exception.RazorpayNotConfiguredException;
import com.abhishek.ecommerce.payment.gateway.razorpay.util.RazorpaySignatureVerifier;
import com.abhishek.ecommerce.payment.repository.PaymentRepository;
import com.abhishek.ecommerce.shared.enums.OrderStatus;
import com.abhishek.ecommerce.shared.enums.PaymentStatus;
import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayPaymentService {

    private final RazorpayProperties razorpayProperties;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean isEnabled() {
        return razorpayProperties.isEnabled();
    }

    @Transactional
    public RazorpayCreateOrderResponseDto createRazorpayOrder(RazorpayCreateOrderRequestDto requestDto) {
        if (!razorpayProperties.isEnabled()) {
            throw new RazorpayNotConfiguredException();
        }

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(requestDto.getOrderId()));
        validateOrderAccess(order);

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment == null) {
            // Defensive: normal flow creates payment when order is placed, but don't crash if missing.
            payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.ONLINE)
                    .status(PaymentStatus.PENDING)
                    .amount(order.getTotalAmount())
                    .transactionId(null)
                    .build();
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Payment already completed for this order");
        }

        // Flip to ONLINE if order was originally created with COD (UI default path)
        payment.setPaymentMethod(PaymentMethod.ONLINE);
        payment.setStatus(PaymentStatus.PENDING);

        long amountPaise = toPaise(order.getTotalAmount() != null ? order.getTotalAmount().getAmount() : BigDecimal.ZERO);
        String currency = order.getTotalAmount() != null ? order.getTotalAmount().getCurrency() : "INR";

        try {
            RazorpayClient client = new RazorpayClient(razorpayProperties.getKeyId(), razorpayProperties.getKeySecret());

            JSONObject options = new JSONObject();
            options.put("amount", amountPaise);
            options.put("currency", currency);
            // Keep receipt deterministic and useful for debugging in Razorpay dashboard
            options.put("receipt", "order_" + order.getId());

            JSONObject notes = new JSONObject();
            notes.put("internal_order_id", String.valueOf(order.getId()));
            if (payment.getId() != null) {
                notes.put("payment_id", String.valueOf(payment.getId()));
            }
            options.put("notes", notes);

            com.razorpay.Order rzOrder = client.orders.create(options);
            String razorpayOrderId = rzOrder.get("id");

            // Store Razorpay order id temporarily in transactionId until verification succeeds.
            // This avoids schema changes while still allowing idempotent verification.
            payment.setTransactionId(razorpayOrderId);
            paymentRepository.save(payment);

            return RazorpayCreateOrderResponseDto.builder()
                    .enabled(true)
                    .keyId(razorpayProperties.getKeyId())
                    .razorpayOrderId(razorpayOrderId)
                    .amount(amountPaise)
                    .currency(currency)
                    .internalOrderId(order.getId())
                    .build();
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for orderId={}: {}", order.getId(), e.getMessage(), e);
            throw new IllegalStateException("Failed to create Razorpay order");
        }
    }

    @Transactional
    public void verifyAndMarkPaid(RazorpayVerifyPaymentRequestDto requestDto) {
        if (!razorpayProperties.isEnabled()) {
            throw new RazorpayNotConfiguredException();
        }

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(requestDto.getOrderId()));
        validateOrderAccess(order);

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalStateException("Payment record not found for this order"));

        if (payment.getStatus() == PaymentStatus.SUCCESS && order.getStatus() == OrderStatus.PAID) {
            // Idempotent success
            return;
        }

        boolean valid = RazorpaySignatureVerifier.verify(
                requestDto.getRazorpayOrderId(),
                requestDto.getRazorpayPaymentId(),
                requestDto.getRazorpaySignature(),
                razorpayProperties.getKeySecret()
        );

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new IllegalStateException("Invalid Razorpay payment signature");
        }

        // Extra safety: ensure the order id we created matches what's being verified
        if (payment.getTransactionId() != null && !payment.getTransactionId().equals(requestDto.getRazorpayOrderId())) {
            throw new IllegalStateException("Razorpay order mismatch for this payment");
        }

        // Atomically update both entities
        payment.setPaymentMethod(PaymentMethod.ONLINE);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(requestDto.getRazorpayPaymentId());

        order.setStatus(OrderStatus.PAID);

        orderRepository.save(order);
        paymentRepository.save(payment);
    }

    private void validateOrderAccess(Order order) {
        String currentUsername = securityUtils.getCurrentUsername();
        if (currentUsername == null) throw new AccessDeniedException("User not authenticated");

        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) return;

        Long currentUserId = securityUtils.getCurrentUserId();
        if (order.getUser() == null || !order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to pay for this order");
        }
    }

    private long toPaise(BigDecimal amount) {
        if (amount == null) return 0L;
        // INR has 2 decimal places; Razorpay expects amount in paise
        return amount.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
    }
}

