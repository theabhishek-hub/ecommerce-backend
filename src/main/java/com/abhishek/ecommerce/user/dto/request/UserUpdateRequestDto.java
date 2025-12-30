package com.abhishek.ecommerce.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {

    private String password;
    @NotBlank(message = "Full name is required")
    private String fullName;
}

