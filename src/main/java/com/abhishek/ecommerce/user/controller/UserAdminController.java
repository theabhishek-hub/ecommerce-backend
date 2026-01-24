package com.abhishek.ecommerce.user.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin Users", description = "Admin-only user management operations")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService userService;

    // ========================= CREATE =========================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new user", description = "Requires ADMIN role")
    public ApiResponse<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateRequestDto requestDto
    ) {
        UserResponseDto response = userService.createUser(requestDto);
        return ApiResponseBuilder.created("User created successfully", response);
    }

    // ========================= GET ALL =========================
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all users", description = "Requires ADMIN role")
    public ApiResponse<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ApiResponseBuilder.success("Users fetched successfully", users);
    }

    // ========================= GET ALL ACTIVE =========================
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all active users", description = "Requires ADMIN role")
    public ApiResponse<List<UserResponseDto>> getAllActiveUsers() {
        List<UserResponseDto> users = userService.getAllActiveUsers();
        return ApiResponseBuilder.success("Active users fetched successfully", users);
    }

    // ========================= UPDATE USER STATUS =========================
    @PutMapping("/{userId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update user status", description = "Requires ADMIN role")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status) {
        userService.updateUserStatus(userId, status);
        return ApiResponseBuilder.success("User status updated successfully", null);
    }

    // ========================= ACTIVATE USER =========================
    @PutMapping("/{userId}/activate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Activate user", description = "Requires ADMIN role")
    public ApiResponse<Void> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ApiResponseBuilder.success("User activated successfully", null);
    }

    // ========================= DEACTIVATE USER =========================
    @PutMapping("/{userId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Deactivate user", description = "Requires ADMIN role")
    public ApiResponse<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ApiResponseBuilder.success("User deactivated successfully", null);
    }

    // ========================= UNLOCK USER =========================
    @PutMapping("/{userId}/unlock")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Unlock user", description = "Requires ADMIN role")
    public ApiResponse<Void> unlockUser(@PathVariable Long userId) {
        userService.unlockUser(userId);
        return ApiResponseBuilder.success("User unlocked successfully", null);
    }

    // ========================= ASSIGN SELLER ROLE =========================
    @PutMapping("/{userId}/roles/seller")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Assign seller role to user", description = "Requires ADMIN role")
    public ApiResponse<Void> assignSellerRole(@PathVariable Long userId) {
        userService.assignSellerRole(userId);
        return ApiResponseBuilder.success("Seller role assigned successfully", null);
    }

    // ========================= REMOVE SELLER ROLE =========================
    @DeleteMapping("/{userId}/roles/seller")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove seller role from user", description = "Requires ADMIN role")
    public ApiResponse<Void> removeSellerRole(@PathVariable Long userId) {
        userService.removeSellerRole(userId);
        return ApiResponseBuilder.success("Seller role removed successfully", null);
    }

    // ========================= SELLER APPLICATION MANAGEMENT =========================

    /**
     * Get all pending seller applications
     */
    @GetMapping("/sellers/pending")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all pending seller applications", description = "Requires ADMIN role")
    public ApiResponse<List<UserResponseDto>> getPendingSellerApplications() {
        List<UserResponseDto> pendingApplications = userService.getPendingSellerApplications();
        return ApiResponseBuilder.success("Pending seller applications fetched successfully", pendingApplications);
    }

    /**
     * Approve seller application
     */
    @PutMapping("/sellers/{userId}/approve")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Approve seller application", description = "Requires ADMIN role")
    public ApiResponse<Void> approveSellerApplication(
            @PathVariable Long userId,
            @RequestParam(required = false) String notes) {
        userService.approveSellerApplication(userId, notes);
        return ApiResponseBuilder.success("Seller application approved successfully", null);
    }

    /**
     * Reject seller application
     */
    @PutMapping("/sellers/{userId}/reject")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reject seller application", description = "Requires ADMIN role")
    public ApiResponse<Void> rejectSellerApplication(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String rejectionReason = body.get("reason");
        userService.rejectSellerApplication(userId, rejectionReason);
        return ApiResponseBuilder.success("Seller application rejected successfully", null);
    }

    /**
     * Suspend seller
     */
    @PutMapping("/sellers/{userId}/suspend")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Suspend seller account", description = "Requires ADMIN role")
    public ApiResponse<Void> suspendSeller(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String suspensionReason = body.get("reason");
        userService.suspendSeller(userId, suspensionReason);
        return ApiResponseBuilder.success("Seller account suspended successfully", null);
    }

    /**
     * Activate suspended seller
     */
    @PutMapping("/sellers/{userId}/activate-suspended")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Activate suspended seller account", description = "Requires ADMIN role")
    public ApiResponse<Void> activateSuspendedSeller(@PathVariable Long userId) {
        userService.activateSuspendedSeller(userId);
        return ApiResponseBuilder.success("Seller account activated successfully", null);
    }
}
