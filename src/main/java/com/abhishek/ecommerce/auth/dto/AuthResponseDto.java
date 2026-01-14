package com.abhishek.ecommerce.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@Schema(description = "Authentication response with JWT token")
public class AuthResponseDto {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "User ID", example = "123")
    private Long userId;

    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @Schema(description = "User roles", example = "[\"USER\"]")
    private Set<String> roles;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "Refresh token", example = "refresh-token-uuid")
    private String refreshToken;

    @Schema(description = "Refresh token expiry time in milliseconds", example = "1640995200000")
    private Long refreshTokenExpiryMs;

    public AuthResponseDto(String token, Long userId, String email, Set<String> roles, String tokenType, String refreshToken, Long refreshTokenExpiryMs) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.tokenType = tokenType;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public static AuthResponseDtoBuilder builder() {
        return new AuthResponseDtoBuilder();
    }

    public static class AuthResponseDtoBuilder {
        private String token;
        private Long userId;
        private String email;
        private Set<String> roles;
        private String tokenType;
        private String refreshToken;
        private Long refreshTokenExpiryMs;

        public AuthResponseDtoBuilder token(String token) {
            this.token = token;
            return this;
        }

        public AuthResponseDtoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuthResponseDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AuthResponseDtoBuilder role(String role) {
            this.roles = Set.of(role);
            return this;
        }

        public AuthResponseDtoBuilder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public AuthResponseDtoBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public AuthResponseDtoBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public AuthResponseDtoBuilder refreshTokenExpiryMs(Long refreshTokenExpiryMs) {
            this.refreshTokenExpiryMs = refreshTokenExpiryMs;
            return this;
        }

        public AuthResponseDto build() {
            AuthResponseDto dto = new AuthResponseDto(token, userId, email, roles, tokenType, refreshToken, refreshTokenExpiryMs);
            return dto;
        }
    }
}
