package com.abhishek.ecommerce.user.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.user.entity.UserStatus;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserService userService;

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status) {
        userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponseBuilder.success(
                "User status updated successfully", null));
    }

    @PutMapping("/{userId}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponseBuilder.success(
                "User unlocked successfully", null));
    }
}

