package com.abhishek.ecommerce.user.service.impl;

import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.UserStatus;
import com.abhishek.ecommerce.user.exception.UserAlreadyExistsException;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
import com.abhishek.ecommerce.user.mapper.UserMapper;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ========================= CREATE =========================
    @Override
    public UserResponseDto createUser(UserCreateRequestDto requestDto) {

        // check duplicate email
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + requestDto.getEmail()
            );
        }

        User user = userMapper.toEntity(requestDto);
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    // ========================= UPDATE =========================
    @Override
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Update only allowed fields
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }


    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {

        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

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

    // ========================= STATUS =========================
    @Override
    @Transactional
    public void activateUser(Long userId) {

        User user = userRepository
                .findByIdAndStatus(userId, UserStatus.INACTIVE)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Inactive user not found with id: " + userId
                        )
                );

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }


    @Override
    public void deactivateUser(Long userId) {
        User user = getUserOrThrow(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    // ========================= DELETE (SOFT) =========================
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    // ========================= PRIVATE HELPER =========================
    private User getUserOrThrow(Long userId) {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }
}




