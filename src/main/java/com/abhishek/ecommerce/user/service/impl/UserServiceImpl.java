package com.abhishek.ecommerce.user.service.impl;

import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserProfileUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.abhishek.ecommerce.user.exception.UserAlreadyExistsException;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
import com.abhishek.ecommerce.user.mapper.UserMapper;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.abhishek.ecommerce.notification.NotificationService;
import com.abhishek.ecommerce.user.entity.SellerApplication;
import com.abhishek.ecommerce.user.repository.SellerApplicationRepository;
import com.abhishek.ecommerce.ui.seller.dto.SellerApplicationRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;
    private final SellerApplicationRepository sellerApplicationRepository;

    // ========================= CREATE =========================
    @Override
    public UserResponseDto createUser(UserCreateRequestDto requestDto) {
        log.info("createUser started for email={}", requestDto.getEmail());

        // check duplicate email
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            log.warn("createUser duplicate email={}", requestDto.getEmail());
            throw new UserAlreadyExistsException(requestDto.getEmail());
        }

        User user = userMapper.toEntity(requestDto);
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        log.info("createUser completed for email={}", requestDto.getEmail());

        // Send welcome email (async side effect)
        notificationService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto findOrCreateOAuthUser(String email, String fullName, String provider) {
        log.info("findOrCreateOAuthUser: Attempting to find or create OAuth user with email={}, provider={}", email, provider);

        // First, check if user already exists with this email
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    log.info("findOrCreateOAuthUser: User already exists for email={}, returning existing user", email);
                    return userMapper.toDto(existingUser);
                })
                .orElseGet(() -> {
                    // User does not exist, create new OAuth user
                    log.info("findOrCreateOAuthUser: Creating new OAuth user with email={}, provider={}", email, provider);
                    
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(fullName);
                    newUser.setRoles(java.util.Set.of(com.abhishek.ecommerce.shared.enums.Role.ROLE_USER));
                    newUser.setStatus(UserStatus.ACTIVE);
                    newUser.setProvider(com.abhishek.ecommerce.shared.enums.AuthProvider.valueOf(provider.toUpperCase()));

                    User savedUser = userRepository.save(newUser);
                    log.info("findOrCreateOAuthUser: Successfully created new OAuth user with email={}, id={}", email, savedUser.getId());

                    return userMapper.toDto(savedUser);
                });
    }

    // ========================= UPDATE =========================
    @Override
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto dto) {
        log.info("updateUser started for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Update only allowed fields
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        User updatedUser = userRepository.save(user);

        log.info("updateUser completed for userId={}", userId);
        return userMapper.toDto(updatedUser);
    }


    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        // CRITICAL FIX: Use fresh query that bypasses Hibernate cache
        // Ensures latest SellerStatus is fetched from database after admin approval
        User user = userRepository.findFreshByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return userMapper.toDto(user);
    }


    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllActiveUsers() {
        return userRepository.findAllByStatus(UserStatus.ACTIVE)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // ========================= PAGINATION & SEARCH =========================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponseDto> content = userPage.getContent()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<UserResponseDto>builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<UserResponseDto> searchUsersByEmail(String email, Pageable pageable) {
        Page<User> userPage = userRepository.searchByEmail(email, pageable);
        List<UserResponseDto> content = userPage.getContent()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<UserResponseDto>builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<UserResponseDto> getUsersBySellerStatus(SellerStatus status, Pageable pageable) {
        Page<User> userPage = userRepository.findBySellerStatus(status, pageable);
        List<UserResponseDto> content = userPage.getContent()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<UserResponseDto>builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();
    }

    // ========================= STATUS =========================
    @Override
    @Transactional
    public void activateUser(Long userId) {
        log.info("activateUser started for userId={}", userId);

        User user = userRepository
                .findByIdAndStatus(userId, UserStatus.INACTIVE)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("activateUser completed for userId={}", userId);
    }


    @Override
    public void deactivateUser(Long userId) {
        log.info("deactivateUser started for userId={}", userId);
        User user = getUserOrThrow(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("deactivateUser completed for userId={}", userId);
    }

    // ========================= DELETE (SOFT) =========================
    @Override
    public void deleteUser(Long userId) {
        log.info("deleteUser started for userId={}", userId);
        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("deleteUser completed for userId={}", userId);
    }

    // ========================= ADMIN OPERATIONS =========================
    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        log.info("updateUserStatus started for userId={}, status={}", userId, status);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setStatus(status);
        userRepository.save(user);
        
        log.info("updateUserStatus completed for userId={}, status={}", userId, status);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        log.info("unlockUser started for userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        
        log.info("unlockUser completed for userId={}", userId);
    }

    // ========================= PROFILE OPERATIONS =========================
    @Override
    public UserResponseDto getCurrentUserProfile() {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        User user = getUserOrThrow(currentUserId);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateCurrentUserProfile(UserProfileUpdateRequestDto requestDto) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        User user = getUserOrThrow(currentUserId);

        // Update only allowed fields
        user.setFullName(requestDto.getFullName());

        User savedUser = userRepository.save(user);
        log.info("updateCurrentUserProfile completed for userId={}", currentUserId);
        return userMapper.toDto(savedUser);
    }

    // ========================= ROLE MANAGEMENT =========================
    @Override
    public void assignSellerRole(Long userId) {
        User user = getUserOrThrow(userId);
        if (user.getRoles().contains(Role.ROLE_SELLER)) {
            throw new IllegalStateException("User already has SELLER role");
        }
        user.getRoles().add(Role.ROLE_SELLER);
        userRepository.save(user);
        log.info("assignSellerRole completed for userId={}", userId);
    }

    @Override
    public void removeSellerRole(Long userId) {
        User user = getUserOrThrow(userId);
        if (!user.getRoles().contains(Role.ROLE_SELLER)) {
            throw new IllegalStateException("User does not have SELLER role");
        }
        user.getRoles().remove(Role.ROLE_SELLER);
        userRepository.save(user);
        log.info("removeSellerRole completed for userId={}", userId);
    }

    // ========================= COUNT OPERATIONS =========================
    @Override
    public long getTotalUserCount() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }

    @Override
    public long getTotalSellerCount() {
        // Count sellers with APPROVED status from User entity
        return userRepository.countBySellerStatus(SellerStatus.APPROVED);
    }

    @Override
    public long getPendingSellerRequestCount() {
        // Count sellers with REQUESTED status from User entity
        return userRepository.countBySellerStatus(SellerStatus.REQUESTED);
    }

    // ========================= PRIVATE HELPER =========================
    private User getUserOrThrow(Long userId) {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    // ========================= SELLER APPLICATION OPERATIONS =========================

    @Override
    public UserResponseDto approveSeller(Long userId, Long adminUserId) {
        log.info("Admin {} approving seller application for user {}", adminUserId, userId);

        User user = getUserOrThrow(userId);
        User admin = getUserOrThrow(adminUserId);

        // Validate admin has ROLE_ADMIN
        if (!admin.getRoles().contains(Role.ROLE_ADMIN)) {
            log.warn("Non-admin user {} attempted to approve seller", adminUserId);
            throw new IllegalStateException("Only admins can approve sellers");
        }

        user.setSellerStatus(SellerStatus.APPROVED);
        user.setSellerApprovedAt(LocalDateTime.now());
        user.setApprovedByAdmin(admin);

        // Assign ROLE_SELLER if not present
        if (!user.getRoles().contains(Role.ROLE_SELLER)) {
            user.getRoles().add(Role.ROLE_SELLER);
        }

        user = userRepository.save(user);
        log.info("Seller {} approved by admin {}", userId, adminUserId);

        // CRITICAL: Also update SellerApplication status to APPROVED
        // User and SellerApplication are separate entities with separate status fields
        var sellerApp = sellerApplicationRepository.findByUserId(userId);
        if (sellerApp.isPresent()) {
            SellerApplication app = sellerApp.get();
            app.setStatus(SellerStatus.APPROVED);
            sellerApplicationRepository.save(app);
            log.info("SellerApplication status updated to APPROVED for userId={}", userId);
        }

        // Refresh user's Spring Security principal if they are currently logged in
        // This ensures their session immediately reflects the new APPROVED status and ROLE_SELLER
        try {
            if (securityUtils.isUserId(userId)) {
                securityUtils.refreshUserPrincipal(userId);
                log.info("Refreshed principal for newly approved seller {}", userId);
            }
        } catch (Exception e) {
            log.warn("Could not refresh principal for user {} after approval (may not be logged in)", userId, e);
        }

        // Send notification
        notificationService.sendSellerApprovedNotification(user.getId(), user.getEmail(), user.getFullName());

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto rejectSeller(Long userId, Long adminUserId, String rejectionReason) {
        log.info("Admin {} rejecting seller application for user {}", adminUserId, userId);

        User user = getUserOrThrow(userId);
        User admin = getUserOrThrow(adminUserId);

        // Validate admin has ROLE_ADMIN
        if (!admin.getRoles().contains(Role.ROLE_ADMIN)) {
            log.warn("Non-admin user {} attempted to reject seller", adminUserId);
            throw new IllegalStateException("Only admins can reject sellers");
        }

        user.setSellerStatus(SellerStatus.REJECTED);
        user.setSellerRejectionReason(rejectionReason);
        user.setApprovedByAdmin(admin);

        // Remove ROLE_SELLER
        user.getRoles().remove(Role.ROLE_SELLER);

        user = userRepository.save(user);
        log.info("Seller {} rejected by admin {}", userId, adminUserId);

        // CRITICAL: Also update SellerApplication status to REJECTED
        var sellerApp = sellerApplicationRepository.findByUserId(userId);
        if (sellerApp.isPresent()) {
            SellerApplication app = sellerApp.get();
            app.setStatus(SellerStatus.REJECTED);
            sellerApplicationRepository.save(app);
            log.info("SellerApplication status updated to REJECTED for userId={}", userId);
        }

        // Send notification
        notificationService.sendSellerRejectedNotification(user.getId(), user.getEmail(), user.getFullName(), rejectionReason);

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto suspendSeller(Long userId, Long adminUserId, String suspensionReason) {
        log.info("Admin {} suspending seller {}", adminUserId, userId);

        User user = getUserOrThrow(userId);
        User admin = getUserOrThrow(adminUserId);

        // Validate admin has ROLE_ADMIN
        if (!admin.getRoles().contains(Role.ROLE_ADMIN)) {
            log.warn("Non-admin user {} attempted to suspend seller", adminUserId);
            throw new IllegalStateException("Only admins can suspend sellers");
        }

        user.setSellerStatus(SellerStatus.SUSPENDED);
        user.setSellerRejectionReason(suspensionReason); // Reuse field for suspension reason
        user.setApprovedByAdmin(admin);

        // Remove ROLE_SELLER
        user.getRoles().remove(Role.ROLE_SELLER);

        user = userRepository.save(user);
        log.info("Seller {} suspended by admin {}", userId, adminUserId);

        // CRITICAL: Also update SellerApplication status to SUSPENDED
        var sellerApp = sellerApplicationRepository.findByUserId(userId);
        if (sellerApp.isPresent()) {
            SellerApplication app = sellerApp.get();
            app.setStatus(SellerStatus.SUSPENDED);
            sellerApplicationRepository.save(app);
            log.info("SellerApplication status updated to SUSPENDED for userId={}", userId);
        }

        // Send notification
        notificationService.sendSellerSuspendedNotification(user.getId(), user.getEmail(), user.getFullName(), suspensionReason);

        return userMapper.toDto(user);
    }

    /**
     * Get pending seller applications
     */
    @Override
    public List<UserResponseDto> getPendingSellerApplications() {
        log.info("getPendingSellerApplications fetching all pending seller applications");
        return userRepository.findBySellerStatus(SellerStatus.REQUESTED)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * User applies to become a seller
     * Sets sellerStatus = REQUESTED and stores form data
     */
    @Override
    public void applySeller(Long userId, Object applicationForm) {
        log.info("applySeller started for userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Set seller status to REQUESTED
        user.setSellerStatus(SellerStatus.REQUESTED);
        user.setSellerRequestedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Save seller application with form data
        if (applicationForm instanceof SellerApplicationRequestDto) {
            SellerApplicationRequestDto formData = (SellerApplicationRequestDto) applicationForm;
            
            // Check if seller application already exists (update case)
            SellerApplication sellerApp = sellerApplicationRepository.findByUserId(userId)
                    .orElse(new SellerApplication());
            
            // Map form data to entity
            sellerApp.setUser(user);
            sellerApp.setBusinessName(formData.getBusinessName());
            sellerApp.setBusinessDescription(formData.getBusinessDescription());
            sellerApp.setPan(formData.getPanNumber());
            sellerApp.setGstNumber(formData.getGstNumber());
            sellerApp.setAddressLine1(formData.getStreetAddress());
            sellerApp.setCity(formData.getCity());
            sellerApp.setState(formData.getState());
            sellerApp.setPostalCode(formData.getPostalCode());
            sellerApp.setCountry(formData.getCountry());
            sellerApp.setPhoneNumber(formData.getPhoneNumber());
            sellerApp.setBankName(""); // Not in form, will be added later if needed
            sellerApp.setAccountHolderName(""); // Not in form, will be added later if needed
            sellerApp.setAccountNumber(maskAccountNumber(formData.getBankAccountNumber()));
            sellerApp.setIfscCode(formData.getBankIfscCode());
            sellerApp.setStatus(SellerStatus.REQUESTED);
            sellerApp.setSubmissionDate(LocalDateTime.now());
            
            sellerApplicationRepository.save(sellerApp);
            log.info("SellerApplication saved for userId={}", userId);
        }
        
        log.info("applySeller completed for userId={}, sellerStatus set to REQUESTED", userId);
    }
    
    /**
     * Mask account number for security
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "**** **** **** " + accountNumber.substring(accountNumber.length() - 4);
    }

    @Override
    public PageResponseDto<UserResponseDto> getAllPendingSellerApplications(Pageable pageable) {
        log.info("Fetching pending seller applications with pagination");
        Page<User> page = userRepository.findBySellerStatus(SellerStatus.REQUESTED, pageable);
        return new PageResponseDto<UserResponseDto>(
                page.stream().map(userMapper::toDto).collect(Collectors.toList()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }

    /**
     * Approve a seller application
     */
    @Override
    public void approveSellerApplication(Long userId, String adminNotes) {
        log.info("approveSellerApplication for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if user has pending seller status
        if (user.getSellerStatus() != SellerStatus.REQUESTED) {
            log.warn("approveSellerApplication invalid status for userId={}, current status={}", userId, user.getSellerStatus());
            throw new IllegalStateException("User seller status must be REQUESTED to approve");
        }

        // Get current admin user
        Long currentAdminId = securityUtils.getCurrentUserId();
        if (currentAdminId == null) {
            throw new IllegalStateException("Admin user not found");
        }
        User currentAdmin = userRepository.findById(currentAdminId)
                .orElseThrow(() -> new UserNotFoundException(currentAdminId));

        // Update seller status
        user.setSellerStatus(SellerStatus.APPROVED);
        user.setSellerApprovedAt(LocalDateTime.now());
        user.setApprovedByAdmin(currentAdmin);

        // Assign ROLE_SELLER if not already assigned
        if (!user.getRoles().contains(Role.ROLE_SELLER)) {
            user.getRoles().add(Role.ROLE_SELLER);
        }

        userRepository.save(user);

        log.info("approveSellerApplication completed for userId={}", userId);

        // Refresh user's Spring Security principal if they are currently logged in
        try {
            if (securityUtils.isUserId(userId)) {
                securityUtils.refreshUserPrincipal(userId);
                log.info("Refreshed principal for newly approved seller {}", userId);
            }
        } catch (Exception e) {
            log.warn("Could not refresh principal for user {} after approval (may not be logged in)", userId, e);
        }

        // Send approval notification
        notificationService.sendSellerApprovedNotification(user.getId(), user.getEmail(), user.getFullName());
    }

    /**
     * Reject a seller application
     */
    @Override
    public void rejectSellerApplication(Long userId, String rejectionReason) {
        log.info("rejectSellerApplication for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if user has pending seller status
        if (user.getSellerStatus() != SellerStatus.REQUESTED) {
            log.warn("rejectSellerApplication invalid status for userId={}, current status={}", userId, user.getSellerStatus());
            throw new IllegalStateException("User seller status must be REQUESTED to reject");
        }

        // Get current admin user
        Long currentAdminId = securityUtils.getCurrentUserId();
        if (currentAdminId == null) {
            throw new IllegalStateException("Admin user not found");
        }
        User currentAdmin = userRepository.findById(currentAdminId)
                .orElseThrow(() -> new UserNotFoundException(currentAdminId));

        // Update seller status
        user.setSellerStatus(SellerStatus.REJECTED);
        user.setSellerRejectionReason(rejectionReason);
        user.setApprovedByAdmin(currentAdmin);

        userRepository.save(user);

        log.info("rejectSellerApplication completed for userId={}", userId);

        // Send rejection notification
        notificationService.sendSellerRejectedNotification(user.getId(), user.getEmail(), user.getFullName(), rejectionReason);
    }

    /**
     * Suspend a seller account
     */
    @Override
    public void suspendSeller(Long userId, String suspensionReason) {
        log.info("suspendSeller for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if user is an approved seller
        if (user.getSellerStatus() != SellerStatus.APPROVED) {
            log.warn("suspendSeller invalid status for userId={}, current status={}", userId, user.getSellerStatus());
            throw new IllegalStateException("User must have APPROVED seller status to suspend");
        }

        // Get current admin user
        Long currentAdminId = securityUtils.getCurrentUserId();
        if (currentAdminId == null) {
            throw new IllegalStateException("Admin user not found");
        }
        User currentAdmin = userRepository.findById(currentAdminId)
                .orElseThrow(() -> new UserNotFoundException(currentAdminId));

        // Update seller status
        user.setSellerStatus(SellerStatus.SUSPENDED);
        user.setSellerRejectionReason(suspensionReason); // Reuse field for suspension reason
        user.setApprovedByAdmin(currentAdmin);

        userRepository.save(user);

        log.info("suspendSeller completed for userId={}", userId);

        // Send suspension notification
        notificationService.sendSellerSuspendedNotification(user.getId(), user.getEmail(), user.getFullName(), suspensionReason);
    }

    /**
     * Activate a suspended seller account
     */
    @Override
    public void activateSuspendedSeller(Long userId) {
        log.info("activateSuspendedSeller for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if user is suspended
        if (user.getSellerStatus() != SellerStatus.SUSPENDED) {
            log.warn("activateSuspendedSeller invalid status for userId={}, current status={}", userId, user.getSellerStatus());
            throw new IllegalStateException("User must have SUSPENDED seller status to activate");
        }

        // Get current admin user
        Long currentAdminId = securityUtils.getCurrentUserId();
        if (currentAdminId == null) {
            throw new IllegalStateException("Admin user not found");
        }
        User currentAdmin = userRepository.findById(currentAdminId)
                .orElseThrow(() -> new UserNotFoundException(currentAdminId));

        // Update seller status back to APPROVED
        user.setSellerStatus(SellerStatus.APPROVED);
        user.setSellerRejectionReason(null); // Clear suspension reason
        user.setApprovedByAdmin(currentAdmin);

        userRepository.save(user);

        log.info("activateSuspendedSeller completed for userId={}", userId);
    }
}

