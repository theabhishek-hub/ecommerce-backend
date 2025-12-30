
package com.abhishek.ecommerce.user.mapper;

import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // ================= CREATE =================
    User toEntity(UserCreateRequestDto dto);

    // ================= RESPONSE =================
    UserResponseDto toDto(User user);

    // ================= UPDATE =================
    void updateEntityFromDto(
            UserUpdateRequestDto dto,
            @MappingTarget User user
    );
}


