package com.abhishek.ecommerce.user.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class UserResponseDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private Set<String> roles;

    // Seller fields
    private String sellerStatus;
    private LocalDateTime sellerRequestedAt;
    private LocalDateTime sellerApprovedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}