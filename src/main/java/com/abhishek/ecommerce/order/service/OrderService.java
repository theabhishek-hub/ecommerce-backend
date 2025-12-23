package com.abhishek.ecommerce.order.service;

import com.abhishek.ecommerce.order.entity.Order;

import java.util.List;

/**
 * Order business logic
 */
public interface OrderService {

    Order placeOrder(Long userId);

    List<Order> getOrdersByUser(Long userId);

    Order getOrderById(Long orderId);
}


