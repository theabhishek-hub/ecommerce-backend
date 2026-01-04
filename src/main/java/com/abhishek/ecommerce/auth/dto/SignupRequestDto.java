package com.abhishek.ecommerce.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequestDto {

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50, message = "Full name must be 3–50 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Full name can contain only letters and spaces"
    )
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be 8–20 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain upper, lower, number, and special character"
    )
    private String password;
}

