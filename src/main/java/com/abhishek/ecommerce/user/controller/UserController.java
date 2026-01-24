package com.abhishek.ecommerce.user.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.user.dto.request.UserProfileUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Users", description = "User profile and account operations")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    // ========================= UPDATE =========================
    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update user", description = "Requires user ownership or ADMIN role")
    @PreAuthorize("isAuthenticated() and (@securityUtils.isUserId(#userId) or hasRole('ADMIN'))")
    public ApiResponse<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto requestDto
    ) {
        UserResponseDto response = userService.updateUser(userId, requestDto);
        return ApiResponseBuilder.success("User updated successfully", response);
    }

    // ========================= GET BY ID =========================
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get user by ID", description = "Requires user ownership or ADMIN role")
    @PreAuthorize("isAuthenticated() and (@securityUtils.isUserId(#userId) or hasRole('ADMIN'))")
    public ApiResponse<UserResponseDto> getUserById(@PathVariable Long userId) {
        UserResponseDto response = userService.getUserById(userId);
        return ApiResponseBuilder.success("User fetched successfully", response);
    }

    // ========================= DELETE (SOFT DELETE) =========================
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete/deactivate user", description = "Requires user ownership or ADMIN role")
    @PreAuthorize("isAuthenticated() and (@securityUtils.isUserId(#userId) or hasRole('ADMIN'))")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponseBuilder.success("User deleted successfully", null);
    }

    // ========================= PROFILE OPERATIONS =========================
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get current user profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponseDto> getCurrentUserProfile() {
        UserResponseDto response = userService.getCurrentUserProfile();
        return ApiResponseBuilder.success("Profile fetched successfully", response);
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update current user profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponseDto> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto
    ) {
        UserResponseDto response = userService.updateCurrentUserProfile(requestDto);
        return ApiResponseBuilder.success("Profile updated successfully", response);
    }

    // ========================= SELLER ROLE OPERATIONS =========================
    // Note: Seller is just a role/status on User entity, not a separate entity
    
    /**
     * GET /api/v1/users/me/seller-status
     * Check if current user's seller application has been approved
     * Used by the approval-pending page to poll for approval status
     * 
     * Returns: {
     *   "status": "success|error",
     *   "data": {
     *     "approved": true|false,
     *     "sellerStatus": "APPROVED|REQUESTED|REJECTED|SUSPENDED|NOT_A_SELLER"
     *   }
     * }
     */
    @GetMapping("/me/seller-status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check seller approval status", description = "Check if current user's seller role is approved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SellerStatusResponse>> checkSellerStatus() {
        try {
            Long userId = securityUtils.getCurrentUserId();
            if (userId == null) {
                log.warn("checkSellerStatus - User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.<SellerStatusResponse>builder()
                                .success(false)
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("User not authenticated")
                                .data(null)
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
            }

            // Get fresh user data from database to check current approval status
            UserResponseDto user = userService.getUserById(userId);
            
            if (user == null) {
                log.warn("checkSellerStatus - User not found for userId={}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<SellerStatusResponse>builder()
                                .success(false)
                                .status(HttpStatus.NOT_FOUND.value())
                                .message("User not found")
                                .data(null)
                                .timestamp(java.time.LocalDateTime.now().toString())
                                .build());
            }

            // Extract seller status - convert string from DTO back to enum for comparison
            String sellerStatusStr = user.getSellerStatus();
            boolean isApproved = "APPROVED".equals(sellerStatusStr);

            log.debug("checkSellerStatus - userId={}, sellerStatus={}, approved={}", 
                    userId, sellerStatusStr, isApproved);

            // Return both the approval status and current seller status
            return ResponseEntity.ok(ApiResponseBuilder.success(
                    "Seller status checked",
                    new SellerStatusResponse(isApproved, sellerStatusStr)
            ));

        } catch (Exception e) {
            log.error("Error checking seller status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SellerStatusResponse>builder()
                            .success(false)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error checking seller status: " + e.getMessage())
                            .data(null)
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }
    }

    /**
     * Response DTO for seller status check endpoint
     */
    public static class SellerStatusResponse {
        private boolean approved;
        private String sellerStatus;

        public SellerStatusResponse(boolean approved, String sellerStatus) {
            this.approved = approved;
            this.sellerStatus = sellerStatus;
        }

        public boolean isApproved() {
            return approved;
        }

        public String getSellerStatus() {
            return sellerStatus;
        }
    }
}
