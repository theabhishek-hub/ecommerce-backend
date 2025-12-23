package com.abhishek.ecommerce.order.service.impl;

import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import com.abhishek.ecommerce.cart.repository.CartRepository;
import com.abhishek.ecommerce.common.entity.Money;
import com.abhishek.ecommerce.order.entity.*;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order business implementation
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    /**
     * Places order from cart
     */
    @Override
    @Transactional
    public Order placeOrder(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order with empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            total = total.add(
                    cartItem.getPrice().getAmount()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );

            order.getItems().add(orderItem);
        }

        order.setTotalAmount(
                new Money(total, "INR")
        );

        // Clear cart after successful order
        cart.getItems().clear();

        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}

