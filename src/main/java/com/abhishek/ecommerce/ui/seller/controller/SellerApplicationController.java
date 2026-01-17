package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.seller.dto.request.SellerApplicationRequestDto;
import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for seller application flow (user side)
 * - APPROVED sellers are redirected to dashboard
 * - REQUESTED/PENDING sellers see status page
 * - NOT_A_SELLER users see application form
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
     * Smart routing based on seller status:
     * - APPROVED → redirect to dashboard
     * - REQUESTED → show pending status
     * - NOT_A_SELLER → show application form
     */
    @GetMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public String showApplyPage(Model model, RedirectAttributes redirectAttributes) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            
            // CRITICAL FIX: Force refresh user data to get latest SellerStatus from database
            var refreshedUser = userService.getUserById(currentUser.getId());
            var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
            
            // CRITICAL: Check seller profile status FIRST (source of truth)
            // If seller profile exists and is APPROVED, redirect immediately
            if (sellerProfile != null && sellerProfile.getStatus() != null) {
                String sellerProfileStatus = sellerProfile.getStatus();
                model.addAttribute("sellerProfile", sellerProfile);
                
                // If seller profile is APPROVED, redirect to dashboard
                if (SellerStatus.APPROVED.name().equals(sellerProfileStatus)) {
                    log.info("Approved seller (from profile) attempted /seller/apply - redirecting to dashboard. UserId={}, SellerId={}", 
                        currentUser.getId(), sellerProfile.getId());
                    return "redirect:/seller/dashboard";
                }
                
                // Update user's sellerStatus from seller profile for consistency
                refreshedUser.setSellerStatus(sellerProfileStatus);
            } else {
                // No seller profile exists, use user's sellerStatus
                if (refreshedUser.getSellerStatus() == null) {
                    refreshedUser.setSellerStatus("NOT_A_SELLER");
                }
            }
            
            // CRITICAL: If already approved (from user status), redirect to dashboard
            if (SellerStatus.APPROVED.name().equals(refreshedUser.getSellerStatus())) {
                log.info("Approved seller attempted /seller/apply - redirecting to dashboard. UserId={}", currentUser.getId());
                return "redirect:/seller/dashboard";
            }
            
            // If REQUESTED, show status page instead of form
            if (SellerStatus.REQUESTED.name().equals(refreshedUser.getSellerStatus())) {
                log.info("Pending seller viewing application status. UserId={}", currentUser.getId());
                model.addAttribute("user", refreshedUser);
                model.addAttribute("title", "Application Status");
                return "seller/application-status";
            }
            
            // Add empty form DTO if not already in model
            if (!model.containsAttribute("applicationForm")) {
                model.addAttribute("applicationForm", new SellerApplicationRequestDto());
            }
            
            model.addAttribute("user", refreshedUser);
            model.addAttribute("title", "Become a Seller");
            
            log.debug("Seller apply page - userId={}, sellerStatus={}", 
                    currentUser.getId(), refreshedUser.getSellerStatus());
            return "seller/apply";
        } catch (Exception e) {
            log.error("Error loading seller apply page", e);
            model.addAttribute("error", "Unable to load page: " + e.getMessage());
            return "seller/apply";
        }
    }

    /**
     * POST /seller/apply
     * Submit seller application with business details
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public String submitApplication(
            @Valid SellerApplicationRequestDto applicationForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                log.warn("Seller application validation errors: {}", bindingResult.getAllErrors());
                bindingResult.getAllErrors().forEach(error -> 
                    model.addAttribute("error", error.getDefaultMessage())
                );
                model.addAttribute("applicationForm", applicationForm);
                return "seller/apply";
            }
            
            var currentUser = userService.getCurrentUserProfile();
            
            // Verify not already approved
            if (SellerStatus.APPROVED.name().equals(currentUser.getSellerStatus())) {
                log.warn("Approved seller tried to resubmit application. UserId={}", currentUser.getId());
                return "redirect:/seller/dashboard";
            }
            
            sellerService.applyForSellerWithDetails(currentUser.getId(), applicationForm);
            redirectAttributes.addFlashAttribute("success", "Seller application submitted successfully! Your account is under review. You will be notified once approved.");
            log.info("Seller application submitted with details for userId={}", currentUser.getId());
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
