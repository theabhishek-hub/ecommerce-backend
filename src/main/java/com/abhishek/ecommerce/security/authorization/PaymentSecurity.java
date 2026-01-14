package com.abhishek.ecommerce.security.authorization;

import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.payment.entity.Payment;
import com.abhishek.ecommerce.payment.repository.PaymentRepository;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("paymentSecurity")
@RequiredArgsConstructor
public class PaymentSecurity {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public boolean isPaymentOwner(Long paymentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        String username = auth.getName();
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null || payment.getOrder() == null) return false;
        Order order = payment.getOrder();
        if (order.getUser() == null) return false;
        Long userId = getUserIdFromUsername(username);
        if (userId == null) return false;
        return order.getUser().getId().equals(userId);
    }

    public boolean isOrderOwnerForPayment(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        String username = auth.getName();
        Long userId = getUserIdFromUsername(username);
        if (userId == null) return false;
        
        // Find payment by orderId and check ownership
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null || payment.getOrder() == null) return false;
        if (payment.getOrder().getUser() == null) return false;
        return payment.getOrder().getUser().getId().equals(userId);
    }

    private Long getUserIdFromUsername(String username) {
        User user = userRepository.findByEmail(username).orElse(null);
        return user != null ? user.getId() : null;
    }
}

