package com.abhishek.ecommerce.order.security;

import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public boolean isOrderOwner(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        String username = auth.getName();
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        Long userId = getUserIdFromUsername(username);
        if (userId == null) return false;
        // Order has a User reference; compare by id
        return order.getUser() != null && order.getUser().getId() != null && order.getUser().getId().equals(userId);
    }

    private Long getUserIdFromUsername(String username) {
        User user = userRepository.findByEmail(username).orElse(null);
        return user != null ? user.getId() : null;
    }
}
