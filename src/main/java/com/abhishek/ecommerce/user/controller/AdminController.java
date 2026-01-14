package com.abhishek.ecommerce.user.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "Admin-only operations")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // ========================= ASSIGN SELLER ROLE =========================
    @PutMapping("/{userId}/roles/seller")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> assignSellerRole(@PathVariable Long userId) {
        userService.assignSellerRole(userId);
        return ApiResponseBuilder.success("Seller role assigned successfully", null);
    }

    // ========================= REMOVE SELLER ROLE =========================
    @DeleteMapping("/{userId}/roles/seller")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> removeSellerRole(@PathVariable Long userId) {
        userService.removeSellerRole(userId);
        return ApiResponseBuilder.success("Seller role removed successfully", null);
    }
}