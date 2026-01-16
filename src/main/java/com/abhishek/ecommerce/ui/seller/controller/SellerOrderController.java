package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Seller Orders Controller
 * Access: ROLE_SELLER with SellerStatus = APPROVED
 * Sellers can view orders for their products only
 */
@Slf4j
@Controller
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final UserService userService;

    /**
     * GET /seller/orders
     * List orders for seller's products
     */
    @GetMapping
    public String listOrders(Model model) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            
            if (currentUser.getSellerStatus() != null && 
                !currentUser.getSellerStatus().equals(SellerStatus.APPROVED.name())) {
                log.warn("Seller orders accessed by non-approved seller");
                model.addAttribute("error", "Your seller account is not approved");
                return "redirect:/seller/dashboard";
            }
            
            // TODO: Load seller's orders
            model.addAttribute("user", currentUser);
            log.info("Seller orders page loaded for userId={}", currentUser.getId());
            return "seller/orders/list";
        } catch (Exception e) {
            log.error("Error loading seller orders", e);
            model.addAttribute("error", "Unable to load orders");
            return "seller/orders/list";
        }
    }
}
