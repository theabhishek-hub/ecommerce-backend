package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Seller Dashboard Controller
 * Access: ROLE_SELLER with SellerStatus = APPROVED
 * Protected by SecurityConfig + method-level checks
 */
@Slf4j
@Controller
@RequestMapping("/seller/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerDashboardController {

    private final UserService userService;

    /**
     * GET /seller/dashboard
     * Display seller dashboard
     */
    @GetMapping
    public String dashboard(Model model) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            
            // Verify seller is approved
            if (currentUser.getSellerStatus() != null && 
                !currentUser.getSellerStatus().equals(SellerStatus.APPROVED.name())) {
                log.warn("Seller dashboard accessed by non-approved seller. Status: {}", currentUser.getSellerStatus());
                model.addAttribute("error", "Your seller account is not approved");
                return "seller/dashboard";
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("title", "Seller Dashboard");
            
            // TODO: Add seller-specific metrics here
            log.info("Seller dashboard loaded for userId={}", currentUser.getId());
            return "seller/dashboard";
        } catch (Exception e) {
            log.error("Error loading seller dashboard", e);
            model.addAttribute("error", "Unable to load dashboard");
            return "seller/dashboard";
        }
    }
}
