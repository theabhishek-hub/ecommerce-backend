package com.abhishek.ecommerce.user.service;

import com.abhishek.ecommerce.notification.NotificationService;
import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.request.UserUpdateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.user.entity.AuthProvider;
import com.abhishek.ecommerce.user.entity.Role;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.UserStatus;
import com.abhishek.ecommerce.user.exception.UserAlreadyExistsException;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
import com.abhishek.ecommerce.user.mapper.UserMapper;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDto userResponseDto;
    private UserCreateRequestDto createRequestDto;
    private UserUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setFullName("John Doe");
        user.setPasswordHash("hashedPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.ROLE_USER);
        user.setProvider(AuthProvider.LOCAL);
        user.setFailedLoginAttempts(0);

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setEmail("john.doe@example.com");
        userResponseDto.setFullName("John Doe");
        userResponseDto.setStatus("ACTIVE");

        createRequestDto = new UserCreateRequestDto();
        createRequestDto.setEmail("john.doe@example.com");
        createRequestDto.setFullName("John Doe");
        createRequestDto.setPassword("password123");

        updateRequestDto = new UserUpdateRequestDto();
        updateRequestDto.setFullName("Updated John Doe");
        updateRequestDto.setPassword("newPassword123");
    }

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(createRequestDto)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.createUser(createRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(userMapper).toEntity(createRequestDto);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(notificationService).sendWelcomeEmail("john.doe@example.com", "John Doe");
        verify(userMapper).toDto(user);
    }

    @Test
    void createUser_ShouldThrowException_WhenUserAlreadyExists() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequestDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("john.doe@example.com");

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    void getUserById_ShouldReturnUser() {
        // Given
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        verify(userRepository).findByIdAndStatus(1L, UserStatus.ACTIVE);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByIdAndStatus(1L, UserStatus.ACTIVE);
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        // When
        List<UserResponseDto> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
        verify(userMapper).toDto(any(User.class));
    }

    @Test
    void getAllActiveUsers_ShouldReturnActiveUsers() {
        // Given
        List<User> users = List.of(user);
        when(userRepository.findAllByStatus(UserStatus.ACTIVE)).thenReturn(users);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        // When
        List<UserResponseDto> result = userService.getAllActiveUsers();

        // Then
        assertThat(result).hasSize(1);
        verify(userRepository).findAllByStatus(UserStatus.ACTIVE);
    }

    @Test
    void updateUser_ShouldUpdateUserSuccessfully() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("john.doe@example.com");
        updatedUser.setFullName("Updated John Doe");
        updatedUser.setPasswordHash("newHashedPassword");
        updatedUser.setStatus(UserStatus.ACTIVE);

        UserResponseDto updatedResponseDto = new UserResponseDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setEmail("john.doe@example.com");
        updatedResponseDto.setFullName("Updated John Doe");
        updatedResponseDto.setStatus("ACTIVE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(any(User.class))).thenReturn(updatedResponseDto);

        // When
        UserResponseDto result = userService.updateUser(1L, updateRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Updated John Doe");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(any(User.class));
    }

    @Test
    void activateUser_ShouldActivateUser() {
        // Given
        User inactiveUser = new User();
        inactiveUser.setId(1L);
        inactiveUser.setEmail("john.doe@example.com");
        inactiveUser.setStatus(UserStatus.INACTIVE);

        when(userRepository.findByIdAndStatus(1L, UserStatus.INACTIVE)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.activateUser(1L);

        // Then
        verify(userRepository).findByIdAndStatus(1L, UserStatus.INACTIVE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deactivateUser_ShouldDeactivateUser() {
        // Given
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        // When
        userService.deactivateUser(1L);

        // Then
        verify(userRepository).findByIdAndStatus(1L, UserStatus.ACTIVE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        // Given
        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findByIdAndStatus(1L, UserStatus.ACTIVE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserStatus_ShouldUpdateUserStatus() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userService.updateUserStatus(1L, UserStatus.INACTIVE);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void unlockUser_ShouldUnlockUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userService.unlockUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }
}