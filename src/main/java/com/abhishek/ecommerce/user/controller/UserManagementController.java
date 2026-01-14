package com.abhishek.ecommerce.user.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserService userService;

    @Operation(
        summary = "Update user status",
        description = "Requires ADMIN role"
    )
    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status) {
        userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponseBuilder.success(
                "User status updated successfully", null));
    }

    @Operation(
        summary = "Unlock user",
        description = "Requires ADMIN role"
    )
    @PutMapping("/{userId}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponseBuilder.success(
                "User unlocked successfully", null));
    }
}

