package com.abhishek.ecommerce.auth.controller;

import com.abhishek.ecommerce.auth.dto.AuthResponseDto;
import com.abhishek.ecommerce.auth.dto.LoginRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupResponseDto;
import com.abhishek.ecommerce.auth.service.AuthService;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private SecurityUtils securityUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private SignupRequestDto signupRequestDto;
    private LoginRequestDto loginRequestDto;
    private SignupResponseDto signupResponseDto;
    private AuthResponseDto authResponseDto;

    @BeforeEach
    void setUp() {
        signupRequestDto = SignupRequestDto.builder()
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        loginRequestDto = LoginRequestDto.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        signupResponseDto = SignupResponseDto.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        authResponseDto = AuthResponseDto.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(900)
                .userId(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role("ROLE_USER")
                .build();
    }

    @Test
    void register_ShouldRegisterUserSuccessfully() throws Exception {
        // Given
        when(authService.signup(any(SignupRequestDto.class))).thenReturn(signupResponseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User"));
    }

    @Test
    void login_ShouldAuthenticateUserSuccessfully() throws Exception {
        // Given
        when(authService.login(any(LoginRequestDto.class))).thenReturn(authResponseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void register_ShouldReturn400_WhenValidationFails() throws Exception {
        // Given - Invalid email
        SignupRequestDto invalidRequest = SignupRequestDto.builder()
                .email("invalid-email")
                .password("pass")
                .fullName("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_ShouldReturn400_WhenValidationFails() throws Exception {
        // Given - Missing password
        LoginRequestDto invalidRequest = LoginRequestDto.builder()
                .email("test@example.com")
                .password("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}