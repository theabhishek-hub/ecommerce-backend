package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Seller Orders Controller
 * Access: ROLE_SELLER with SellerStatus = APPROVED ONLY
 * Unapproved sellers are redirected to approval pending page
 * Sellers can view orders for their products only
 */
@Slf4j
@Controller
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final UserService userService;
    private final SellerService sellerService;
    private final OrderService orderService;

    /**
     * GET /seller/orders
     * List orders for seller's products
     * BLOCKS access if seller is NOT APPROVED
     */
    @GetMapping
    public String listOrders(Model model, RedirectAttributes redirectAttributes) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            
            // CRITICAL FIX: Force refresh user data to get latest SellerStatus from database
            var refreshedUser = userService.getUserById(currentUser.getId());
            
            // CRITICAL: Check seller profile status FIRST (source of truth)
            var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
            String status = null;
            
            if (sellerProfile != null && sellerProfile.getStatus() != null) {
                // Use seller profile status as source of truth
                status = sellerProfile.getStatus();
                refreshedUser.setSellerStatus(status);
            } else {
                // Fallback to user's sellerStatus if no seller profile exists
                status = refreshedUser.getSellerStatus();
            }
            
            // Check seller approval status
            if (status == null || !SellerStatus.APPROVED.name().equals(status)) {
                log.warn("Unapproved seller attempted orders access. UserId={}, Status={}", 
                    currentUser.getId(), status);
                model.addAttribute("error", "Your seller account must be approved before accessing this page.");
                model.addAttribute("user", refreshedUser);
                model.addAttribute("title", "Access Denied");
                return "seller/approval-pending";
            }
            
            // Fetch orders for seller's products
            List<OrderResponseDto> orders = orderService.getOrdersForSeller(currentUser.getId());
            
            model.addAttribute("user", refreshedUser);
            model.addAttribute("title", "My Orders");
            model.addAttribute("orders", orders);
            model.addAttribute("hasOrders", !orders.isEmpty());
            
            log.info("Seller orders page loaded for userId={}, Orders count={}", 
                currentUser.getId(), orders.size());
            return "seller/orders/list";
        } catch (Exception e) {
            log.error("Error loading seller orders", e);
            model.addAttribute("error", "Unable to load orders");
            return "seller/orders/list";
        }
    }
}
