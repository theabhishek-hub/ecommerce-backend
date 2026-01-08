package com.abhishek.ecommerce.order.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.common.api.PageResponseDto;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST APIs for order management
 */
@Tag(name = "Orders", description = "Order lifecycle operations")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ========================= PLACE ORDER =========================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OrderResponseDto> placeOrder() {
        OrderResponseDto response = orderService.placeOrderForCurrentUser();
        return ApiResponseBuilder.created("Order placed successfully", response);
    }

    // ========================= GET USER ORDERS =========================
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<OrderResponseDto>> getUserOrders() {
        List<OrderResponseDto> orders = orderService.getOrdersForCurrentUser();
        return ApiResponseBuilder.success("User orders fetched successfully", orders);
    }

    // ========================= GET USER ORDERS PAGINATED =========================
    @GetMapping("/paged")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponseDto<OrderResponseDto>> getUserOrdersPaged(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponseDto<OrderResponseDto> orders = orderService.getOrdersForCurrentUser(pageable);
        return ApiResponseBuilder.success("User orders fetched successfully", orders);
    }

    // ========================= GET ORDER BY ID =========================
    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and (@orderSecurity.isOrderOwner(#orderId) or hasRole('ADMIN'))")
    public ApiResponse<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);
        return ApiResponseBuilder.success("Order fetched successfully", response);
    }

    // ========================= SHIP ORDER =========================
    @PutMapping("/{orderId}/ship")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponseDto> shipOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.shipOrder(orderId);
        return ApiResponseBuilder.success("Order shipped successfully", response);
    }

    // ========================= DELIVER ORDER =========================
    @PutMapping("/{orderId}/deliver")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponseDto> deliverOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.deliverOrder(orderId);
        return ApiResponseBuilder.success("Order delivered successfully", response);
    }

    // ========================= CANCEL ORDER =========================
    @PutMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOrderOwner(#orderId)")
    public ApiResponse<OrderResponseDto> cancelOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.cancelOrder(orderId);
        return ApiResponseBuilder.success("Order cancelled successfully", response);
    }
}
