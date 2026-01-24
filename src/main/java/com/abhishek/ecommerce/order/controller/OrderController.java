package com.abhishek.ecommerce.order.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final UserService userService;

    // ========================= PLACE ORDER =========================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OrderResponseDto> placeOrder(
            @RequestParam(value = "paymentMethod", required = false, defaultValue = "COD") String paymentMethod,
            @RequestParam(value = "selectedProductIds", required = false) List<Long> selectedProductIds) {
        // Parse payment method, default to COD for backward compatibility
        com.abhishek.ecommerce.payment.entity.PaymentMethod method;
        try {
            method = com.abhishek.ecommerce.payment.entity.PaymentMethod.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            method = com.abhishek.ecommerce.payment.entity.PaymentMethod.COD;
        }
        
        OrderResponseDto response;
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            response = orderService.placeOrderForCurrentUser(method, selectedProductIds);
        } else {
            response = orderService.placeOrderForCurrentUser(method);
        }
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

    // ========================= SELLER: CONFIRM ORDER =========================
    @Operation(
        summary = "Confirm order (seller fulfillment)",
        description = "Seller confirms order - transitions from PAID to CONFIRMED status"
    )
    @PutMapping("/{orderId}/confirm-seller")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SELLER') and @sellerSecurity.isSellerInOrder(#orderId)")
    public ApiResponse<OrderResponseDto> confirmOrderBySeller(@PathVariable Long orderId) {
        var currentUser = userService.getCurrentUserProfile();
        OrderResponseDto response = orderService.confirmOrder(orderId, currentUser.getId());
        return ApiResponseBuilder.success("Order confirmed successfully", response);
    }

    // ========================= SELLER: SHIP ORDER =========================
    @Operation(
        summary = "Ship order (seller fulfillment)",
        description = "Seller ships order - transitions from CONFIRMED to SHIPPED status"
    )
    @PutMapping("/{orderId}/ship-seller")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SELLER') and @sellerSecurity.isSellerInOrder(#orderId)")
    public ApiResponse<OrderResponseDto> shipOrderBySeller(@PathVariable Long orderId) {
        var currentUser = userService.getCurrentUserProfile();
        OrderResponseDto response = orderService.shipOrderBySeller(orderId, currentUser.getId());
        return ApiResponseBuilder.success("Order shipped successfully", response);
    }

    // ========================= SELLER: DELIVER ORDER =========================
    @Operation(
        summary = "Deliver order (seller fulfillment)",
        description = "Seller marks order as delivered - transitions from SHIPPED to DELIVERED status"
    )
    @PutMapping("/{orderId}/deliver-seller")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SELLER') and @sellerSecurity.isSellerInOrder(#orderId)")
    public ApiResponse<OrderResponseDto> deliverOrderBySeller(@PathVariable Long orderId) {
        var currentUser = userService.getCurrentUserProfile();
        OrderResponseDto response = orderService.deliverOrderBySeller(orderId, currentUser.getId());
        return ApiResponseBuilder.success("Order delivered successfully", response);
    }
}
