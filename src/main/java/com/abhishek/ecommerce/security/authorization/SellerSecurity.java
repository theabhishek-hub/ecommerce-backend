package com.abhishek.ecommerce.security.authorization;

import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security checks for seller-specific resource access in Spring EL expressions
 * Used in @PreAuthorize annotations for REST API endpoints
 */
@Component("sellerSecurity")
@RequiredArgsConstructor
public class SellerSecurity {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * Check if current authenticated user is the seller who owns the product
     * @param productId Product ID to check ownership
     * @return true if current user is the seller of this product, false otherwise
     */
    public boolean isSellerOwnerProduct(Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String username = auth.getName();
        Long userId = getUserIdFromUsername(username);
        if (userId == null) {
            return false;
        }

        // Check if seller owns this product (userId is seller ID in consolidated model)
        return productRepository.existsByIdAndSellerId(productId, userId);
    }

    /**
     * Check if current authenticated user is a seller who has products in this order
     * (i.e., at least one product in the order belongs to this seller)
     * @param orderId Order ID to check
     * @return true if current user is a seller with products in this order, false otherwise
     */
    public boolean isSellerInOrder(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String username = auth.getName();
        Long userId = getUserIdFromUsername(username);
        if (userId == null) {
            return false;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // Check if any order item has a product owned by this seller (userId is seller ID)
        return order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSeller() != null && item.getProduct().getSeller().getId().equals(userId));
    }

    private Long getUserIdFromUsername(String username) {
        User user = userRepository.findByEmail(username).orElse(null);
        return user != null ? user.getId() : null;
    }
}
