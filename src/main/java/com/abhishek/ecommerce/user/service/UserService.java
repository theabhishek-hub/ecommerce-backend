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

    /**
     * Find existing OAuth user by email OR create new OAuth user.
     * This method ensures email uniqueness to prevent duplicate user creation.
     * 
     * @param email OAuth user email
     * @param fullName OAuth user full name
     * @param provider OAuth provider (e.g., GOOGLE, LOCAL)
     * @return Existing or newly created user
     */
    UserResponseDto findOrCreateOAuthUser(String email, String fullName, String provider);

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

    // ========================= SELLER APPLICATION OPERATIONS =========================
    /**
     * Admin approves a seller application
     * Sets sellerStatus = APPROVED
     * Assigns ROLE_SELLER to user
     */
    UserResponseDto approveSeller(Long userId, Long adminUserId);

    /**
     * Admin rejects a seller application
     * Sets sellerStatus = REJECTED
     * Removes ROLE_SELLER from user
     */
    UserResponseDto rejectSeller(Long userId, Long adminUserId, String rejectionReason);

    /**
     * Admin suspends an approved seller
     * Sets sellerStatus = SUSPENDED
     * Removes ROLE_SELLER from user
     */
    UserResponseDto suspendSeller(Long userId, Long adminUserId, String suspensionReason);

    /**
     * Get all pending seller applications
     */
    List<UserResponseDto> getPendingSellerApplications();

    /**
     * Submit seller application with business details
     * User applies to become a seller
     */
    void applySeller(Long userId, Object applicationForm);

    /**
     * Approve a seller application by admin
     * Sets sellerStatus = APPROVED
     * Assigns ROLE_SELLER to user
     */
    void approveSellerApplication(Long userId, String adminNotes);

    /**
     * Reject a seller application by admin
     * Sets sellerStatus = REJECTED
     * Keeps ROLE_SELLER unless removed
     */
    void rejectSellerApplication(Long userId, String rejectionReason);

    /**
     * Suspend a seller account by admin
     * Sets sellerStatus = SUSPENDED
     */
    void suspendSeller(Long userId, String suspensionReason);

    /**
     * Activate a suspended seller account
     * Sets sellerStatus = APPROVED
     */
    void activateSuspendedSeller(Long userId);

    /**
     * Get all pending seller applications with pagination
     */
    PageResponseDto<UserResponseDto> getAllPendingSellerApplications(Pageable pageable);
}
