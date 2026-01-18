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
import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.abhishek.ecommerce.notification.NotificationService;

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
    private final SellerService sellerService;

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
        // Count sellers with APPROVED status
        return sellerService.countByStatus(SellerStatus.APPROVED);
    }

    @Override
    public long getPendingSellerRequestCount() {
        // Count sellers with REQUESTED status using SellerService
        return sellerService.countByStatus(SellerStatus.REQUESTED);
    }

    // ========================= PRIVATE HELPER =========================
    private User getUserOrThrow(Long userId) {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}

