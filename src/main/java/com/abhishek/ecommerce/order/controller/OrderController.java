package com.abhishek.ecommerce.order.controller;

import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST APIs for order management
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Place order from cart
     */
    @PostMapping("/place/{userId}")
    public Order placeOrder(@PathVariable Long userId) {
        return orderService.placeOrder(userId);
    }

    /**
     * Get all orders of a user
     */
    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }
}

