package com.abhishek.ecommerce.auth.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Data
@Schema(description = "Refresh token request payload")
public class RefreshRequestDto {

    @Schema(description = "Refresh token obtained during login", example = "refresh-token-uuid", requiredMode = RequiredMode.REQUIRED)
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

