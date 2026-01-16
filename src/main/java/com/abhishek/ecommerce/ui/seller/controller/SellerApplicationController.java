package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for seller application flow (user side)
 * - View seller apply page
 * - Submit application
 */
@Slf4j
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerApplicationController {

    private final UserService userService;
    private final SellerService sellerService;

    /**
     * GET /seller/apply
     * Display seller application page
     */
    @GetMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public String showApplyPage(Model model) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
            model.addAttribute("user", currentUser);
            model.addAttribute("seller", sellerProfile);
            if (sellerProfile != null) {
                model.addAttribute("sellerStatus", sellerProfile.getStatus());
            }
        } catch (Exception e) {
            log.error("Error loading seller apply page", e);
            model.addAttribute("error", "Unable to load page");
        }
        return "seller/apply";
    }

    /**
     * POST /seller/apply
     * Submit seller application
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public String submitApplication(RedirectAttributes redirectAttributes) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            sellerService.applyForSeller(currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Seller application submitted successfully. Please wait for admin approval.");
            log.info("Seller application submitted successfully for userId={}", currentUser.getId());
            return "redirect:/seller/apply";
        } catch (IllegalStateException e) {
            log.warn("Seller application error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/apply";
        } catch (Exception e) {
            log.error("Error submitting seller application", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred. Please try again.");
            return "redirect:/seller/apply";
        }
    }
}
