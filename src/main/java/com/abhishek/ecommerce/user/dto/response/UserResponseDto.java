package com.abhishek.ecommerce.user.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserResponseDto {

    private Long id;
    private String fullName;
    private String email;
    private String status;
    private Set<String> roles;

}

