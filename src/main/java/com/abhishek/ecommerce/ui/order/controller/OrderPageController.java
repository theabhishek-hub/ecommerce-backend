package com.abhishek.ecommerce.ui.order.controller;

import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * UI Controller for order management pages.
 * Session-based authentication (NOT JWT).
 * Renders Thymeleaf templates for authenticated users.
 */
@Slf4j
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderPageController {

    private final OrderService orderService;

    /**
     * Display list of all orders for authenticated user
     * @param model Thymeleaf model
     * @return orders/list template
     */
    @GetMapping
    public String ordersList(Model model) {
        try {
            // Get all orders for currently authenticated user
            List<OrderResponseDto> orders = orderService.getOrdersForCurrentUser();
            
            model.addAttribute("title", "My Orders");
            model.addAttribute("orders", orders);
            model.addAttribute("hasOrders", !orders.isEmpty());
            
            log.info("Loaded {} orders for authenticated user", orders.size());
            return "orders/list";
        } catch (Exception e) {
            log.error("Error loading orders list", e);
            model.addAttribute("errorMessage", "Unable to load orders. Please try again later.");
            return "orders/list";
        }
    }

    /**
     * Display details of specific order
     * @param orderId Order ID
     * @param model Thymeleaf model
     * @return orders/details template
     */
    @GetMapping("/{orderId}")
    public String orderDetails(@PathVariable Long orderId, Model model) {
        try {
            // Get order details
            OrderResponseDto order = orderService.getOrderById(orderId);
            
            // Verify order belongs to current user (in service layer)
            // OrderService should validate this internally
            
            model.addAttribute("title", "Order Details");
            model.addAttribute("order", order);
            
            log.info("Loaded details for order: {}", orderId);
            return "orders/details";
        } catch (OrderNotFoundException e) {
            log.warn("Order not found: {}", orderId);
            model.addAttribute("errorMessage", "Order not found.");
            return "orders/list";
        } catch (Exception e) {
            log.error("Error loading order details for orderId: {}", orderId, e);
            model.addAttribute("errorMessage", "Unable to load order details. Please try again later.");
            return "orders/list";
        }
    }
}
