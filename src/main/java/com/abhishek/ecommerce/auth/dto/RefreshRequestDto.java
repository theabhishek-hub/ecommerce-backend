package com.abhishek.ecommerce.auth.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Refresh token request payload")
public class RefreshRequestDto {

    @Schema(description = "Refresh token obtained during login", example = "refresh-token-uuid", required = true)
    private String refreshToken;
}

