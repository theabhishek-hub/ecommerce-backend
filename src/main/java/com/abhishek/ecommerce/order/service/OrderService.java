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
     * Place order with specified payment method
     */
    OrderResponseDto placeOrder(Long userId, com.abhishek.ecommerce.payment.entity.PaymentMethod paymentMethod);

    /**
     * Place order with specified payment method and selected products
     */
    OrderResponseDto placeOrder(Long userId, com.abhishek.ecommerce.payment.entity.PaymentMethod paymentMethod, List<Long> selectedProductIds);

    /**
     * Place order for the current authenticated user
     */
    OrderResponseDto placeOrderForCurrentUser();

    /**
     * Place order for current user with specified payment method
     */
    OrderResponseDto placeOrderForCurrentUser(com.abhishek.ecommerce.payment.entity.PaymentMethod paymentMethod);

    /**
     * Place order for current user with specified payment method and selected products
     */
    OrderResponseDto placeOrderForCurrentUser(com.abhishek.ecommerce.payment.entity.PaymentMethod paymentMethod, List<Long> selectedProductIds);

    List<OrderResponseDto> getOrdersByUser(Long userId);

    PageResponseDto<OrderResponseDto> getOrdersByUser(Long userId, Pageable pageable);

    /**
     * Get orders for the current authenticated user
     */
    List<OrderResponseDto> getOrdersForCurrentUser();

    PageResponseDto<OrderResponseDto> getOrdersForCurrentUser(Pageable pageable);

    OrderResponseDto getOrderById(Long orderId);
    List<OrderResponseDto> getAllOrders();
    PageResponseDto<OrderResponseDto> getAllOrders(Pageable pageable);
    OrderResponseDto shipOrder(Long orderId);

    OrderResponseDto deliverOrder(Long orderId);

    OrderResponseDto cancelOrder(Long orderId);

    /**
     * Admin/Seller confirms payment (transitions CREATED -> PAID)
     */
    OrderResponseDto confirmPayment(Long orderId);

    /**
     * Admin confirms order (transitions PAID -> CONFIRMED)
     */
    OrderResponseDto confirmOrderAsAdmin(Long orderId);

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

    /**
     * Seller confirms/accepts an order (transitions PAID -> CONFIRMED)
     * Only seller who owns products in the order can confirm
     */
    OrderResponseDto confirmOrder(Long orderId, Long sellerId);

    /**
     * Seller ships order (transitions CONFIRMED -> SHIPPED)
     * Only seller who owns products in the order can ship
     */
    OrderResponseDto shipOrderBySeller(Long orderId, Long sellerId);

    /**
     * Seller marks order as delivered (transitions SHIPPED -> DELIVERED)
     * Only seller who owns products in the order can deliver
     */
    OrderResponseDto deliverOrderBySeller(Long orderId, Long sellerId);
}