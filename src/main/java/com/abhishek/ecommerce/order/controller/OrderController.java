package com.abhishek.ecommerce.order.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST APIs for order management
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ========================= PLACE ORDER =========================
    @PostMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponseDto> placeOrder(@PathVariable Long userId) {
        OrderResponseDto response = orderService.placeOrder(userId);
        return ApiResponseBuilder.created("Order placed successfully", response);
    }

    // ========================= GET USER ORDERS =========================
    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<OrderResponseDto>> getUserOrders(@PathVariable Long userId) {
        List<OrderResponseDto> orders = orderService.getOrdersByUser(userId);
        return ApiResponseBuilder.success("User orders fetched successfully", orders);
    }

    // ========================= GET ORDER BY ID =========================
    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);
        return ApiResponseBuilder.success("Order fetched successfully", response);
    }

    // ========================= SHIP ORDER =========================
    @PutMapping("/{orderId}/ship")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderResponseDto> shipOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.shipOrder(orderId);
        return ApiResponseBuilder.success("Order shipped successfully", response);
    }

    // ========================= DELIVER ORDER =========================
    @PutMapping("/{orderId}/deliver")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderResponseDto> deliverOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.deliverOrder(orderId);
        return ApiResponseBuilder.success("Order delivered successfully", response);
    }

    // ========================= CANCEL ORDER =========================
    @PutMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderResponseDto> cancelOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.cancelOrder(orderId);
        return ApiResponseBuilder.success("Order cancelled successfully", response);
    }
}

