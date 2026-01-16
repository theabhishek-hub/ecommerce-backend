package com.abhishek.ecommerce.order.service;

import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import org.springframework.data.domain.Pageable;

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

    PageResponseDto<OrderResponseDto> getOrdersByUser(Long userId, Pageable pageable);

    /**
     * Get orders for the current authenticated user
     */
    List<OrderResponseDto> getOrdersForCurrentUser();

    PageResponseDto<OrderResponseDto> getOrdersForCurrentUser(Pageable pageable);

    OrderResponseDto getOrderById(Long orderId);
    List<OrderResponseDto> getAllOrders();
    OrderResponseDto shipOrder(Long orderId);

    OrderResponseDto deliverOrder(Long orderId);

    OrderResponseDto cancelOrder(Long orderId);

    // COUNT OPERATIONS
    long getTotalOrderCount();

    long getPendingOrderCount();

    // SELLER OPERATIONS
    /**
     * Get all orders containing items from a specific seller's products
     */
    List<OrderResponseDto> getOrdersForSeller(Long sellerId);

    /**
     * Get paginated orders for a seller
     */
    PageResponseDto<OrderResponseDto> getOrdersForSeller(Long sellerId, Pageable pageable);
}