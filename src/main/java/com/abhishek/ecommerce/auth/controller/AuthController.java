package com.abhishek.ecommerce.auth.controller;

import com.abhishek.ecommerce.auth.dto.AuthResponseDto;
import com.abhishek.ecommerce.auth.dto.LoginRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupResponseDto;
import com.abhishek.ecommerce.auth.service.AuthService;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.common.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Authentication", description = "JWT login, refresh token, OAuth2 login")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final SecurityUtils securityUtils;

    // ============================
    // SIGNUP (REGISTER)
    // ============================
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with email and password"
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            @Valid @RequestBody SignupRequestDto request
    ) {
        log.info("Signup attempt for email={}", request.getEmail());

        SignupResponseDto response = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseBuilder.created(
                                "User registered successfully",
                                response
                        )
                );
    }

    // ============================
    // LOGIN
    // ============================
    @Operation(
        summary = "User login",
        description = "Authenticates user and returns JWT access token with refresh token"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        log.info("Login attempt for email={}", request.getEmail());

        AuthResponseDto response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponseBuilder.success(
                        "Login successful",
                        response
                )
        );
    }

    // ============================
    // LOGOUT (revoke refresh token)
    // ============================
    @Operation(
        summary = "User logout",
        description = "Revokes the refresh token for the current user session"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        String username = securityUtils.getCurrentUsername();
        if (username != null) {
            refreshTokenService.deleteByUsername(username);
        }
        return ResponseEntity.ok(ApiResponseBuilder.success("Logged out successfully", null));
    }
}
