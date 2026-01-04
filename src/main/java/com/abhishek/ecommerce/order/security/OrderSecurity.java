package com.abhishek.ecommerce.order.security;

import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;

    public boolean isOrderOwner(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        String username = auth.getName();
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        return order.getUserId().equals(getUserIdFromUsername(username));
    }

    // Simple lookup: find user id by username (email). To avoid cycle, repository call is omitted.
    private Long getUserIdFromUsername(String username) {
        // TODO: use UserRepository to find id; to avoid circular imports, do a simple approach
        try {
            return Long.parseLong(username);
        } catch (Exception ex) {
            return null;
        }
    }
}

