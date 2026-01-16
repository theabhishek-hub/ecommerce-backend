package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Order Oversight Controller
 * ROLE_ADMIN only - enforced by SecurityConfig
 * View all orders, update order status
 */
@Slf4j
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * List all orders from all users
     */
    @GetMapping
    public String ordersList(Model model) {
        try {
            List<OrderResponseDto> orders = orderService.getAllOrders();

            model.addAttribute("title", "Order Oversight");
            model.addAttribute("orders", orders);
            model.addAttribute("hasOrders", !orders.isEmpty());

            log.info("Admin loaded all orders - Total: {}", orders.size());
            return "admin/orders/list";
        } catch (Exception e) {
            log.error("Error loading orders list", e);
            model.addAttribute("errorMessage", "Unable to load orders. Please try again.");
            return "admin/orders/list";
        }
    }

    /**
     * View order details
     */
    @GetMapping("/{orderId}")
    public String orderDetails(@PathVariable Long orderId, Model model) {
        try {
            OrderResponseDto order = orderService.getOrderById(orderId);

            model.addAttribute("title", "Order Details");
            model.addAttribute("order", order);

            log.info("Admin viewed order details: {}", orderId);
            return "admin/orders/details";
        } catch (Exception e) {
            log.error("Error loading order details: {}", orderId, e);
            model.addAttribute("errorMessage", "Order not found.");
            return "redirect:/admin/orders?error=not_found";
        }
    }

    /**
     * Ship order
     */
    @PostMapping("/{orderId}/ship")
    public String shipOrder(@PathVariable Long orderId) {
        try {
            orderService.shipOrder(orderId);
            log.info("Admin shipped order: {}", orderId);
            return "redirect:/admin/orders/{orderId}?success=shipped";
        } catch (Exception e) {
            log.error("Error shipping order: {}", orderId, e);
            return "redirect:/admin/orders/{orderId}?error=ship_failed";
        }
    }

    /**
     * Deliver order
     */
    @PostMapping("/{orderId}/deliver")
    public String deliverOrder(@PathVariable Long orderId) {
        try {
            orderService.deliverOrder(orderId);
            log.info("Admin delivered order: {}", orderId);
            return "redirect:/admin/orders/{orderId}?success=delivered";
        } catch (Exception e) {
            log.error("Error delivering order: {}", orderId, e);
            return "redirect:/admin/orders/{orderId}?error=deliver_failed";
        }
    }

    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            log.info("Admin cancelled order: {}", orderId);
            return "redirect:/admin/orders/{orderId}?success=cancelled";
        } catch (Exception e) {
            log.error("Error cancelling order: {}", orderId, e);
            return "redirect:/admin/orders/{orderId}?error=cancel_failed";
        }
    }
}
