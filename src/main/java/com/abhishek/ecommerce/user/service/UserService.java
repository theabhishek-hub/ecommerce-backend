package com.abhishek.ecommerce.user.service;

import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserProfileUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    // CREATE
    UserResponseDto createUser(UserCreateRequestDto requestDto);

    // UPDATE
    UserResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto);

    // READ
    UserResponseDto getUserById(Long userId);

    List<UserResponseDto> getAllUsers();

    List<UserResponseDto> getAllActiveUsers();

    // ========================= PAGINATION & SEARCH =========================
    /**
     * Get all users with pagination
     */
    PageResponseDto<UserResponseDto> getAllUsers(Pageable pageable);

    /**
     * Search users by email with pagination
     */
    PageResponseDto<UserResponseDto> searchUsersByEmail(String email, Pageable pageable);

    /**
     * Get users by seller status with pagination
     */
    PageResponseDto<UserResponseDto> getUsersBySellerStatus(SellerStatus status, Pageable pageable);

    // STATUS OPERATIONS
    void activateUser(Long userId);

    void deactivateUser(Long userId);

    // DELETE (soft delete)
    void deleteUser(Long userId);

    // PROFILE OPERATIONS
    UserResponseDto getCurrentUserProfile();

    UserResponseDto updateCurrentUserProfile(UserProfileUpdateRequestDto requestDto);

    // ROLE MANAGEMENT
    void assignSellerRole(Long userId);

    void removeSellerRole(Long userId);

    // COUNT OPERATIONS
    long getTotalUserCount();

    long getTotalSellerCount();

    long getPendingSellerRequestCount();

    // ADMIN OPERATIONS
    void updateUserStatus(Long userId, UserStatus status);

    void unlockUser(Long userId);
}
