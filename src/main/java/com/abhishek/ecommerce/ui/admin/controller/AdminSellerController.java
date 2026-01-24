package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.user.repository.SellerApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ROLE_ADMIN only - enforced by SecurityConfig
 * Admin panel for managing seller applications and seller accounts
 * 
 * NOTE: Migrated from SellerService to UserService for seller management (Phase 2)
 */
@Slf4j
@Controller
@RequestMapping("/admin/sellers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final UserService userService;
    private final SellerApplicationRepository sellerApplicationRepository;

    /**
     * GET /admin/sellers
     * List all pending seller requests
     */
    @GetMapping
    public String listSellerRequests(Model model) {
        try {
            var sellerRequests = userService.getPendingSellerApplications();
            model.addAttribute("title", "Seller Management");
            model.addAttribute("sellerRequests", sellerRequests);
            model.addAttribute("requestCount", sellerRequests.size());
            model.addAttribute("hasPendingRequests", !sellerRequests.isEmpty());
            log.info("Admin loaded seller requests - Total: {}", sellerRequests.size());
        } catch (Exception e) {
            log.error("Error loading seller requests", e);
            model.addAttribute("title", "Seller Management");
            model.addAttribute("error", "Unable to load seller requests");
            model.addAttribute("hasPendingRequests", false);
        }
        return "admin/sellers/list";
    }

    /**
     * GET /admin/sellers/{sellerId}/details
     * View seller application details
     */
    @GetMapping("/{sellerId}/details")
    public String viewSellerDetails(@PathVariable Long sellerId, Model model) {
        try {
            var seller = userService.getUserById(sellerId);
            model.addAttribute("seller", seller);
            
            // Fetch seller application details
            var sellerApp = sellerApplicationRepository.findByUserId(sellerId);
            if (sellerApp.isPresent()) {
                model.addAttribute("sellerApplication", sellerApp.get());
            }
            
            log.info("Admin viewed seller details for userId={}", sellerId);
        } catch (Exception e) {
            log.error("Error loading seller details for userId={}", sellerId, e);
            model.addAttribute("error", "Seller not found");
        }
        return "admin/sellers/details";
    }

    /**
     * POST /admin/sellers/{sellerId}/approve
     * Approve seller application
     */
    @PostMapping("/{sellerId}/approve")
    public String approveSeller(@PathVariable Long sellerId, RedirectAttributes redirectAttributes) {
        try {
            var currentAdmin = userService.getCurrentUserProfile();
            userService.approveSeller(sellerId, currentAdmin.getId());
            redirectAttributes.addFlashAttribute("success", "Seller approved successfully");
            log.info("Seller approved by admin for userId={}", sellerId);
            return "redirect:/admin/sellers";
        } catch (Exception e) {
            log.error("Error approving seller for userId={}", sellerId, e);
            redirectAttributes.addFlashAttribute("error", "Error approving seller: " + e.getMessage());
            return "redirect:/admin/sellers";
        }
    }

    /**
     * POST /admin/sellers/{sellerId}/reject
     * Reject seller application
     */
    @PostMapping("/{sellerId}/reject")
    public String rejectSeller(@PathVariable Long sellerId, @RequestParam(required = false) String reason, RedirectAttributes redirectAttributes) {
        try {
            var currentAdmin = userService.getCurrentUserProfile();
            userService.rejectSeller(sellerId, currentAdmin.getId(), reason);
            redirectAttributes.addFlashAttribute("success", "Seller application rejected");
            log.info("Seller rejected by admin for userId={}", sellerId);
            return "redirect:/admin/sellers";
        } catch (Exception e) {
            log.error("Error rejecting seller for userId={}", sellerId, e);
            redirectAttributes.addFlashAttribute("error", "Error rejecting seller: " + e.getMessage());
            return "redirect:/admin/sellers";
        }
    }

    /**
     * POST /admin/sellers/{sellerId}/suspend
     * Suspend seller account
     */
    @PostMapping("/{sellerId}/suspend")
    public String suspendSeller(@PathVariable Long sellerId, @RequestParam(required = false) String reason, RedirectAttributes redirectAttributes) {
        try {
            var currentAdmin = userService.getCurrentUserProfile();
            userService.suspendSeller(sellerId, currentAdmin.getId(), reason);
            redirectAttributes.addFlashAttribute("success", "Seller account suspended");
            log.info("Seller suspended by admin for userId={}", sellerId);
            return "redirect:/admin/sellers";
        } catch (Exception e) {
            log.error("Error suspending seller for userId={}", sellerId, e);
            redirectAttributes.addFlashAttribute("error", "Error suspending seller: " + e.getMessage());
            return "redirect:/admin/sellers";
        }
    }
}

