package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;

/**
 * UI Controller for seller order management pages.
 * Handles seller's order fulfillment views and status updates.
 * 
 * Seller can:
 * - View orders containing their products
 * - Update order status (CONFIRMED, SHIPPED, DELIVERED)
 * - Cannot view/modify orders they don't have items in
 */
@Slf4j
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final OrderService orderService;

    /**
     * Display seller's orders listing page.
     * Shows all orders containing this seller's products.
     * Only accessible to APPROVED sellers.
     */
    @GetMapping("/orders")
    public String listOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "status", required = false) String status,
            Model model) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Allow pending sellers with warning flag
        boolean isPending = !SellerStatus.APPROVED.equals(seller.getSellerStatus());

        try {
            PageResponseDto<OrderResponseDto> pageResponse = orderService.getOrdersForSeller(userId, pageable);
            
            // Filter by status if provided
            List<OrderResponseDto> orders = pageResponse.getContent();
            if (status != null && !status.isEmpty()) {
                orders = orders.stream()
                    .filter(order -> order.getStatus().equalsIgnoreCase(status))
                    .toList();
            }

            model.addAttribute("title", "My Orders");
            model.addAttribute("user", seller);
            model.addAttribute("sellerId", userId);
            model.addAttribute("orders", orders);
            model.addAttribute("page", pageResponse);
            model.addAttribute("filterStatus", status);
            model.addAttribute("hasOrders", !orders.isEmpty());
            model.addAttribute("isPending", isPending);
            model.addAttribute("status", seller.getSellerStatus());
            
            log.info("Seller {} loaded orders - Total: {}", userId, orders.size());
            return "seller/orders/list";
        } catch (Exception e) {
            log.error("Error loading seller orders for userId={}: {}", userId, e.getMessage(), e);
            model.addAttribute("title", "My Orders");
            model.addAttribute("user", seller);
            model.addAttribute("sellerId", userId);
            model.addAttribute("orders", java.util.Collections.emptyList());
            model.addAttribute("hasOrders", false);
            model.addAttribute("isPending", isPending);
            model.addAttribute("status", seller.getSellerStatus());
            model.addAttribute("errorMessage", "Unable to load orders. Please try again.");
            return "seller/orders/list";
        }
    }

    /**
     * Display order details page.
     * Shows full order information including seller's items.
     * Only accessible if seller has items in the order.
     */
    @GetMapping("/orders/{orderId}")
    public String viewOrderDetail(
            @PathVariable Long orderId,
            Model model) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Only approved sellers can view orders
        if (!SellerStatus.APPROVED.equals(seller.getSellerStatus())) {
            model.addAttribute("user", seller);
            model.addAttribute("status", seller.getSellerStatus());
            return "seller/approval-pending";
        }

        // Fetch and display order details
        try {
            OrderResponseDto order = orderService.getOrderById(orderId);
            if (order == null) {
                model.addAttribute("error", "Order not found");
                model.addAttribute("title", "Order Details");
                return "seller/orders/details";
            }
            
            int totalItems = order.getItems() != null ? order.getItems().size() : 0;
            
            model.addAttribute("title", "Order Details");
            model.addAttribute("user", seller);
            model.addAttribute("orderId", orderId);
            model.addAttribute("order", order);
            model.addAttribute("totalItems", totalItems);
            
            log.info("Seller {} viewed order details: {}", userId, orderId);
            return "seller/orders/details";
        } catch (AccessControlException | AccessDeniedException e) {
            log.warn("Access denied for seller {} viewing order {}: {}", userId, orderId, e.getMessage());
            model.addAttribute("error", "You don't have permission to view this order. Only approved sellers can view orders containing their products.");
            model.addAttribute("title", "Order Details");
            return "seller/orders/details";
        } catch (Exception e) {
            log.error("Error loading order {} for seller {}: {}", orderId, userId, e.getMessage(), e);
            model.addAttribute("error", "Unable to load order details: " + e.getMessage());
            model.addAttribute("title", "Order Details");
            return "seller/orders/details";
        }
    }

    /**
     * Confirm payment for an order.
     * Transitions order status from CREATED -> PAID
     * Accessible to any seller/admin.
     */
    @PostMapping("/orders/{orderId}/confirm-payment")
    public String confirmPayment(
            @PathVariable Long orderId,
            RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.confirmPayment(orderId);
            redirectAttributes.addFlashAttribute("success", "Payment confirmed successfully");
        } catch (Exception e) {
            log.error("Error confirming payment for order {}: {}", orderId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to confirm payment: " + e.getMessage());
        }

        return "redirect:/seller/orders/" + orderId;
    }

    /**
     * Confirm/Accept an order.
     * Transitions order status from PAID -> CONFIRMED
     * Only seller with items in the order can confirm.
     */
    @PostMapping("/orders/{orderId}/confirm")
    public String confirmOrder(
            @PathVariable Long orderId,
            RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.confirmOrder(orderId, userId);
            redirectAttributes.addFlashAttribute("success", "Order confirmed successfully");
        } catch (AccessControlException e) {
            log.warn("Seller {} attempted to confirm order {} without authorization", userId, orderId);
            redirectAttributes.addFlashAttribute("error", "You do not have permission to confirm this order");
        } catch (Exception e) {
            log.error("Error confirming order {} for seller {}: {}", orderId, userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to confirm order: " + e.getMessage());
        }

        return "redirect:/seller/orders/" + orderId;
    }

    /**
     * Ship an order.
     * Transitions order status from CONFIRMED -> SHIPPED
     * Only seller with items in the order can ship.
     */
    @PostMapping("/orders/{orderId}/ship")
    public String shipOrder(
            @PathVariable Long orderId,
            @RequestParam(value = "trackingNumber", required = false) String trackingNumber,
            RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.shipOrderBySeller(orderId, userId);
            // TODO: Store tracking number if provided in order entity
            redirectAttributes.addFlashAttribute("success", "Order shipped successfully");
        } catch (AccessControlException e) {
            log.warn("Seller {} attempted to ship order {} without authorization", userId, orderId);
            redirectAttributes.addFlashAttribute("error", "You do not have permission to ship this order");
        } catch (Exception e) {
            log.error("Error shipping order {} for seller {}: {}", orderId, userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to ship order: " + e.getMessage());
        }

        return "redirect:/seller/orders/" + orderId;
    }

    /**
     * Mark order as delivered.
     * Transitions order status from SHIPPED -> DELIVERED
     * Only seller with items in the order can deliver.
     */
    @PostMapping("/orders/{orderId}/deliver")
    public String deliverOrder(
            @PathVariable Long orderId,
            RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        try {
            orderService.deliverOrderBySeller(orderId, userId);
            redirectAttributes.addFlashAttribute("success", "Order delivered successfully");
        } catch (AccessControlException e) {
            log.warn("Seller {} attempted to deliver order {} without authorization", userId, orderId);
            redirectAttributes.addFlashAttribute("error", "You do not have permission to deliver this order");
        } catch (Exception e) {
            log.error("Error delivering order {} for seller {}: {}", orderId, userId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to deliver order: " + e.getMessage());
        }

        return "redirect:/seller/orders/" + orderId;
    }

    // ==================== API ENDPOINTS (JSON RESPONSE) ====================

    /**
     * API endpoint to confirm payment (AJAX).
     * Returns JSON response for frontend.
     */
    @PostMapping("/orders/{orderId}/confirm-payment-api")
    public ResponseEntity<Map<String, Object>> confirmPaymentApi(@PathVariable Long orderId) {
        Long userId = securityUtils.getCurrentUserId();
        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        try {
            orderService.confirmPayment(orderId);
            response.put("success", true);
            response.put("message", "Payment confirmed successfully");
            response.put("status", "PAID");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error confirming payment for order {}: {}", orderId, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to confirm payment: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * API endpoint to confirm order (AJAX).
     * Returns JSON response for frontend.
     */
    @PostMapping("/orders/{orderId}/confirm-api")
    public ResponseEntity<Map<String, Object>> confirmOrderApi(@PathVariable Long orderId) {
        Long userId = securityUtils.getCurrentUserId();
        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        try {
            orderService.confirmOrder(orderId, userId);
            response.put("success", true);
            response.put("message", "Order confirmed successfully");
            response.put("status", "CONFIRMED");
            return ResponseEntity.ok(response);
        } catch (AccessControlException | AccessDeniedException e) {
            log.warn("Seller {} attempted to confirm order {} without authorization", userId, orderId);
            response.put("success", false);
            response.put("message", "You do not have permission to confirm this order");
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            log.error("Error confirming order {} for seller {}: {}", orderId, userId, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to confirm order: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * API endpoint to ship order (AJAX).
     * Returns JSON response for frontend.
     */
    @PostMapping("/orders/{orderId}/ship-api")
    public ResponseEntity<Map<String, Object>> shipOrderApi(@PathVariable Long orderId) {
        Long userId = securityUtils.getCurrentUserId();
        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        try {
            orderService.shipOrderBySeller(orderId, userId);
            response.put("success", true);
            response.put("message", "Order shipped successfully");
            response.put("status", "SHIPPED");
            return ResponseEntity.ok(response);
        } catch (AccessControlException | AccessDeniedException e) {
            log.warn("Seller {} attempted to ship order {} without authorization", userId, orderId);
            response.put("success", false);
            response.put("message", "You do not have permission to ship this order");
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            log.error("Error shipping order {} for seller {}: {}", orderId, userId, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to ship order: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * API endpoint to deliver order (AJAX).
     * Returns JSON response for frontend.
     */
    @PostMapping("/orders/{orderId}/deliver-api")
    public ResponseEntity<Map<String, Object>> deliverOrderApi(@PathVariable Long orderId) {
        Long userId = securityUtils.getCurrentUserId();
        Map<String, Object> response = new HashMap<>();

        if (userId == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        try {
            orderService.deliverOrderBySeller(orderId, userId);
            response.put("success", true);
            response.put("message", "Order delivered successfully");
            response.put("status", "DELIVERED");
            return ResponseEntity.ok(response);
        } catch (AccessControlException | AccessDeniedException e) {
            log.warn("Seller {} attempted to deliver order {} without authorization", userId, orderId);
            response.put("success", false);
            response.put("message", "You do not have permission to deliver this order");
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            log.error("Error delivering order {} for seller {}: {}", orderId, userId, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to deliver order: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
}
