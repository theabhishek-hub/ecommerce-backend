package com.abhishek.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Authentication response with JWT token")
public class AuthResponseDto {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "User ID", example = "123")
    private Long userId;

    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @Schema(description = "User role", example = "USER")
    private String role;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "Refresh token", example = "refresh-token-uuid")
    private String refreshToken;

    @Schema(description = "Refresh token expiry time in milliseconds", example = "1640995200000")
    private Long refreshTokenExpiryMs;
}
