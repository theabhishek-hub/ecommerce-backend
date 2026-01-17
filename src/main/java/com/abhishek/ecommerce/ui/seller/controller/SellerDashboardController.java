package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Seller Dashboard Controller
 * Access: ROLE_SELLER with SellerStatus = APPROVED ONLY
 * Unapproved sellers are redirected to approval pending page
 */
@Slf4j
@Controller
@RequestMapping("/seller/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerDashboardController {

    private final UserService userService;
    private final SellerService sellerService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * GET /seller/dashboard
     * Display seller dashboard with KPI metrics
     * - If APPROVED: shows dashboard with KPI cards
     * - Otherwise: shows approval-pending page (no redirect to avoid loops)
     */
    @GetMapping
    public String dashboard(Model model) {
        try {
            // CRITICAL FIX: Get current user ID and fetch fresh from DB each time
            // This ensures SellerStatus reflects admin approval immediately
            var currentUser = userService.getCurrentUserProfile();
            
            // Force refresh user data from database to get latest SellerStatus
            // In case admin approved seller while they were in another view
            var refreshedUser = userService.getUserById(currentUser.getId());
            
            // CRITICAL: Check seller profile status FIRST (source of truth)
            // Seller profile status is updated by admin approval, so it's most reliable
            var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
            String status = null;
            
            if (sellerProfile != null && sellerProfile.getStatus() != null) {
                // Use seller profile status as source of truth
                status = sellerProfile.getStatus();
                // Also update user's sellerStatus for consistency
                refreshedUser.setSellerStatus(status);
            } else {
                // Fallback to user's sellerStatus if no seller profile exists
                status = refreshedUser.getSellerStatus();
            }
            
            // If APPROVED, show dashboard with KPI
            if (status != null && status.equals(SellerStatus.APPROVED.name())) {
                long totalProducts = productRepository.countBySellerId(currentUser.getId());
                long totalOrders = orderRepository.countBySellerIdWithProducts(currentUser.getId());
                long pendingOrders = orderRepository.countPendingOrdersBySellerID(currentUser.getId());
                
                model.addAttribute("user", refreshedUser);
                model.addAttribute("title", "Seller Dashboard");
                model.addAttribute("totalProducts", totalProducts);
                model.addAttribute("totalOrders", totalOrders);
                model.addAttribute("pendingOrders", pendingOrders);
                
                // Add success message if redirected from approval
                if (model.asMap().get("message") == null) {
                    model.addAttribute("success", "Welcome! Your seller account is now approved and active.");
                }
                
                log.info("Seller dashboard loaded for userId={}. Products={}, Orders={}, Pending={}", 
                    currentUser.getId(), totalProducts, totalOrders, pendingOrders);
                return "seller/dashboard";
            }
            
            // For any non-approved status (REQUESTED, NOT_A_SELLER, REJECTED, SUSPENDED), 
            // show approval-pending page. SellerApplicationController handles the routing logic.
            model.addAttribute("user", refreshedUser);
            model.addAttribute("title", "Seller Account");
            log.info("Non-approved seller accessing dashboard. UserId={}, Status={}", 
                currentUser.getId(), status);
            return "seller/approval-pending";
            
        } catch (Exception e) {
            log.error("Error loading seller dashboard", e);
            model.addAttribute("error", "Unable to load dashboard. Please try again.");
            model.addAttribute("title", "Dashboard Error");
            return "seller/dashboard";
        }
    }

}
