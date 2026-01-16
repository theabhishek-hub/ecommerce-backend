
package com.abhishek.ecommerce.user.mapper;

import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    // ================= CREATE =================
    public User toEntity(UserCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPasswordHash(dto.getPassword());
        user.setRoles(Set.of(Role.ROLE_USER)); // Default role

        return user;
    }

    // ================= RESPONSE =================
    public UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        dto.setRoles(user.getRoles() != null ? user.getRoles().stream()
                .map(role -> role.name().replace("ROLE_", ""))
                .collect(Collectors.toSet()) : null);
        
        // Seller fields
        dto.setSellerStatus(user.getSellerStatus() != null ? user.getSellerStatus().name() : null);
        dto.setSellerRequestedAt(user.getSellerRequestedAt());
        dto.setSellerApprovedAt(user.getSellerApprovedAt());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }

    // ================= UPDATE =================
    public void updateEntityFromDto(UserUpdateRequestDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPassword() != null) {
            user.setPasswordHash(dto.getPassword());
        }
    }
}


