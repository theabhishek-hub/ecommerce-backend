package com.abhishek.ecommerce.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "User registration request payload")
public class SignupRequestDto {

    @Schema(description = "User's full name", example = "John Doe", required = true)
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50, message = "Full name must be 3–50 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Full name can contain only letters and spaces"
    )
    private String fullName;

    @Schema(description = "User email address", example = "john.doe@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "User password (must contain upper, lower, number, and special character)", example = "Password123!", required = true)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be 8–20 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain upper, lower, number, and special character"
    )
    private String password;
}

