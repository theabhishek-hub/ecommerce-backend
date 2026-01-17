package com.abhishek.ecommerce.seller.controller;

import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API Controller for seller status checks
 * Used by approval-pending.html to auto-check approval status
 */
@Slf4j
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerStatusApiController {

    private final UserService userService;

    /**
     * GET /api/seller/check-approval
     * Returns the current seller's approval status
     * Used by approval-pending.html for real-time status checking
     * Accessible to any authenticated user (not just ROLE_SELLER)
     */
    @GetMapping("/check-approval")
    public ResponseEntity<Map<String, String>> checkApprovalStatus() {
        try {
            var currentUser = userService.getCurrentUserProfile();
            String status = currentUser.getSellerStatus();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", status != null ? status : "NOT_A_SELLER");
            response.put("userId", String.valueOf(currentUser.getId()));
            
            log.debug("User {} checked seller approval status: {}", currentUser.getId(), status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking seller approval status", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unable to check status");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
