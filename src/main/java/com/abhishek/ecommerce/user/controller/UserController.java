package com.abhishek.ecommerce.user.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "User profile and account operations")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ========================= CREATE =========================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateRequestDto requestDto
    ) {
        UserResponseDto response = userService.createUser(requestDto);
        return ApiResponseBuilder.created("User created successfully", response);
    }

    // ========================= UPDATE =========================
    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and (@securityUtils.isUserId(#userId) or hasRole('ADMIN'))")
    public ApiResponse<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto requestDto
    ) {
        UserResponseDto response = userService.updateUser(userId, requestDto);
        return ApiResponseBuilder.success("User updated successfully", response);
    }

    // ========================= GET BY ID =========================
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and (@securityUtils.isUserId(#userId) or hasRole('ADMIN'))")
    public ApiResponse<UserResponseDto> getUserById(@PathVariable Long userId) {
        UserResponseDto response = userService.getUserById(userId);
        return ApiResponseBuilder.success("User fetched successfully", response);
    }

    // ========================= GET ALL =========================
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ApiResponseBuilder.success("Users fetched successfully", users);
    }

    // ========================= GET ALL ACTIVE =========================
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponseDto>> getAllActiveUsers() {
        List<UserResponseDto> users = userService.getAllActiveUsers();
        return ApiResponseBuilder.success("Active users fetched successfully", users);
    }

    // ========================= ACTIVATE =========================
    @PutMapping("/{userId}/activate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ApiResponseBuilder.success("User activated successfully", null);
    }

    // ========================= DEACTIVATE =========================
    @PutMapping("/{userId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and (@securityUtils.isUserId(#userId) or hasRole('ADMIN'))")
    public ApiResponse<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ApiResponseBuilder.success("User deactivated successfully", null);
    }

    // ========================= DELETE (SOFT DELETE) =========================
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and (@securityUtils.isUserId(#userId) or hasRole('ADMIN'))")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponseBuilder.success("User deleted successfully", null);
    }
}
