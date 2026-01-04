package com.abhishek.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SignupResponseDto {
    private Long id;
    private String email;
    private String role;
}

