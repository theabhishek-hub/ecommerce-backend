package com.abhishek.ecommerce.order.service;

import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;

import java.util.List;

/**
 * Order business logic
 */
public interface OrderService {

    OrderResponseDto placeOrder(Long userId);

    List<OrderResponseDto> getOrdersByUser(Long userId);

    OrderResponseDto getOrderById(Long orderId);

    OrderResponseDto shipOrder(Long orderId);

    OrderResponseDto deliverOrder(Long orderId);

    OrderResponseDto cancelOrder(Long orderId);
}


