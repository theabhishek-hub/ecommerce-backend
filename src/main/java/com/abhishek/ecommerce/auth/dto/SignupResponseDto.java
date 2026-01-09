package com.abhishek.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "User registration response")
public class SignupResponseDto {

    @Schema(description = "User ID", example = "123")
    private Long id;

    @Schema(description = "User email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User role", example = "USER")
    private String role;
}

