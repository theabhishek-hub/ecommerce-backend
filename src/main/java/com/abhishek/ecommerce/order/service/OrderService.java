package com.abhishek.ecommerce.order.service;

import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;

import java.util.List;

/**
 * Order business logic
 */
public interface OrderService {

    OrderResponseDto placeOrder(Long userId);

    /**
     * Place order for the current authenticated user
     */
    OrderResponseDto placeOrderForCurrentUser();

    List<OrderResponseDto> getOrdersByUser(Long userId);

    /**
     * Get orders for the current authenticated user
     */
    List<OrderResponseDto> getOrdersForCurrentUser();

    OrderResponseDto getOrderById(Long orderId);

    OrderResponseDto shipOrder(Long orderId);

    OrderResponseDto deliverOrder(Long orderId);

    OrderResponseDto cancelOrder(Long orderId);
}


