package com.abhishek.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OAuthResponseDto {
    private String email;
    private String token;
    private String message;
}

