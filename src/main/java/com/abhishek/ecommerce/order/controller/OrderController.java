package com.abhishek.ecommerce.order.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.service.OrderService;
import org.springframework.http.HttpStatus;
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Order> placeOrder(@PathVariable Long userId) {
        Order order = orderService.placeOrder(userId);
        return ApiResponseBuilder.success("Order placed successfully", order);
    }

    /**
     * Get all orders of a user
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<List<Order>> getUserOrders(@PathVariable Long userId) {
        return ApiResponseBuilder.success("User orders fetched successfully", orderService.getOrdersByUser(userId));
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ApiResponse<Order> getOrderById(@PathVariable Long orderId)
    {
        return ApiResponseBuilder.success("Order fetched successfully", orderService.getOrderById(orderId));
    }

    @PatchMapping("/{orderId}/ship")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return ApiResponseBuilder.success("Order shipped successfully");
    }

    @PatchMapping("/{orderId}/deliver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deliverOrder(@PathVariable Long orderId) {
        orderService.deliverOrder(orderId);
        return ApiResponseBuilder.success("Order delivered successfully");
    }

    @PatchMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ApiResponseBuilder.success("Order cancelled successfully");
    }


}

