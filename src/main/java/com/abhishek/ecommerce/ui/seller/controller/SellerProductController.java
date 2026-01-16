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
 * Seller Products Controller
 * Access: ROLE_SELLER with SellerStatus = APPROVED
 * Sellers can view/manage only their own products
 */
@Slf4j
@Controller
@RequestMapping("/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {

    private final UserService userService;

    /**
     * GET /seller/products
     * List seller's products
     */
    @GetMapping
    public String listProducts(Model model) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            
            if (!SellerStatus.APPROVED.name().equals(currentUser.getSellerStatus())) {
                log.warn("Seller products accessed by non-approved seller");
                model.addAttribute("error", "Your seller account is not approved");
                return "redirect:/seller/dashboard";
            }
            
            // TODO: Load seller's products
            model.addAttribute("user", currentUser);
            log.info("Seller products page loaded for userId={}", currentUser.getId());
            return "seller/products/list";
        } catch (Exception e) {
            log.error("Error loading seller products", e);
            model.addAttribute("error", "Unable to load products");
            return "seller/products/list";
        }
    }
}
