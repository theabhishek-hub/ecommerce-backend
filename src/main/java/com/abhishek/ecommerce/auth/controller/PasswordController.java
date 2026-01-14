package com.abhishek.ecommerce.auth.controller;

import com.abhishek.ecommerce.auth.dto.PasswordChangeRequestDto;
import com.abhishek.ecommerce.auth.dto.PasswordResetConfirmDto;
import com.abhishek.ecommerce.auth.dto.PasswordResetRequestDto;
import com.abhishek.ecommerce.auth.service.PasswordService;
import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Password", description = "Authenticated password change")
@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @Operation(
        summary = "Change password",
        description = "Changes the authenticated user's password"
    )
    @PostMapping("/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequestDto request) {
        passwordService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponseBuilder.success("Password changed successfully", null));
    }

    @Operation(
        summary = "Request password reset",
        description = "Sends password reset email to the provided email address"
    )
    @PostMapping("/reset-request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto request) {
        passwordService.requestPasswordReset(request.getEmail());
        // Don't reveal if email exists (security best practice)
        return ResponseEntity.ok(ApiResponseBuilder.success(
                "If the email exists, a password reset link has been sent", null));
    }

    @Operation(
        summary = "Reset password",
        description = "Resets password using the reset token received via email"
    )
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmDto request) {
        passwordService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponseBuilder.success("Password reset successfully", null));
    }
}

