package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.repository.SellerApplicationRepository;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UI Controller for seller dashboard pages.
 * Displays seller metrics, KPIs, and key information.
 */
@Slf4j
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final SellerApplicationRepository sellerApplicationRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    /**
     * Display seller dashboard with KPIs and metrics.
     * Only accessible to APPROVED sellers.
     * Pending sellers are redirected to approval-pending page.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Show approval warning for pending sellers, but allow access
        boolean isPending = !SellerStatus.APPROVED.equals(seller.getSellerStatus());
        model.addAttribute("isPending", isPending);
        model.addAttribute("status", seller.getSellerStatus());

        // Load dashboard data
        model.addAttribute("title", "Seller Dashboard");
        model.addAttribute("user", seller);
        model.addAttribute("seller", seller);
        
        // Load seller application (optional - contains business details like businessName, businessDescription)
        // SellerApplication is NOT required - seller is just a role/status on User entity
        var sellerApplication = sellerApplicationRepository.findByUserId(userId);
        sellerApplication.ifPresent(app -> model.addAttribute("sellerApplication", app));
        
        // Fetch seller KPIs using User entity directly (seller is just a role, not a separate entity)
        try {
            // Get total products for this seller (using userId directly - Product.seller is User)
            long totalProducts = productRepository.countBySellerId(userId);
            
            // Get all orders containing this seller's products
            var sellerOrders = orderService.getOrdersForSeller(userId);
            
            // Get total orders count
            long totalOrders = sellerOrders.size();
            
            // Get pending orders (PAID status - awaiting seller confirmation)
            long pendingOrders = sellerOrders.stream()
                    .filter(order -> order.getStatus() != null && order.getStatus().equals("PAID"))
                    .count();
            
            // Get recent orders (limit to 5)
            var recentOrders = sellerOrders.stream().limit(5).collect(java.util.stream.Collectors.toList());
            
            // Calculate total revenue (sum of delivered orders)
            BigDecimal totalRevenue = sellerOrders.stream()
                    .filter(order -> order.getStatus() != null && 
                            (order.getStatus().equals("DELIVERED") || 
                             order.getStatus().equals("SHIPPED")))
                    .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("pendingOrders", pendingOrders);
            model.addAttribute("totalRevenue", "$" + totalRevenue.setScale(2, java.math.RoundingMode.HALF_UP));
            model.addAttribute("recentOrders", recentOrders);
        } catch (Exception e) {
            log.error("Error loading seller dashboard metrics for user {}", userId, e);
            model.addAttribute("totalProducts", 0);
            model.addAttribute("totalOrders", 0);
            model.addAttribute("pendingOrders", 0);
            model.addAttribute("totalRevenue", "$0.00");
            model.addAttribute("recentOrders", List.of());
        }

        return "seller/dashboard";
    }

    /**
     * DEBUG ENDPOINT: Display seller status and roles
     * Helps diagnose login redirect and navbar issues
     * Shows: Current user, roles, seller status, approved status
     */
    @GetMapping("/debug-status")
    public String debugStatus(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        String username = securityUtils.getCurrentUsername();
        
        model.addAttribute("title", "Seller Debug Status");
        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUsername", username);
        
        // Get authentication details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            model.addAttribute("isAuthenticated", auth.isAuthenticated());
            model.addAttribute("authPrincipal", auth.getPrincipal());
            model.addAttribute("authRoles", auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList()));
        }
        
        // Get user from database
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                model.addAttribute("userEmail", user.getEmail());
                model.addAttribute("userRoles", user.getRoles().stream()
                        .map(r -> r.name())
                        .collect(Collectors.toList()));
                model.addAttribute("sellerStatus", user.getSellerStatus());
                model.addAttribute("isSellerApproved", SellerStatus.APPROVED.equals(user.getSellerStatus()));
                model.addAttribute("isSellerApprovedMethod", securityUtils.isSellerApproved());
            }
        }
        
        return "seller/debug-status";
    }
}
