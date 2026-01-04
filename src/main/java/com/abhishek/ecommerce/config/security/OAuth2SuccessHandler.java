package com.abhishek.ecommerce.config.security;

import com.abhishek.ecommerce.auth.dto.AuthResponseDto;
import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.user.entity.AuthProvider;
import com.abhishek.ecommerce.user.entity.Role;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.UserStatus;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken authToken =
                (OAuth2AuthenticationToken) authentication;

        OAuth2User oauth2User = authToken.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setFullName(name);
                    u.setRole(Role.ROLE_USER);
                    u.setProvider(AuthProvider.GOOGLE);
                    u.setStatus(UserStatus.ACTIVE);
                    return userRepository.save(u);
                });

        // ✅ JWT
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // ✅ Refresh token
        com.abhishek.ecommerce.auth.entity.RefreshToken refreshTokenEntity = 
                refreshTokenService.createOrReplaceRefreshToken(user);
        String refreshToken = refreshTokenEntity.getToken();

        AuthResponseDto authResponseDto = AuthResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name().replace("ROLE_", "")) // Convert ROLE_USER to USER
                .tokenType("Bearer")
                .refreshTokenExpiryMs(refreshTokenEntity.getExpiresAt().toEpochMilli())
                .build();

        // Wrap in ApiResponse format
        ApiResponse<AuthResponseDto> apiResponse = ApiResponse.<AuthResponseDto>builder()
                .success(true)
                .status(HttpServletResponse.SC_OK)
                .message("OAuth login successful")
                .data(authResponseDto)
                .timestamp(LocalDateTime.now().toString())
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter()
                .write(objectMapper.writeValueAsString(apiResponse));
    }

    // ✅ HELPER METHOD — INSIDE THE SAME CLASS
    private User createOAuth2User(String email, String name) {
        User user = new User();

        user.setEmail(email);
        user.setFullName(name);

        // 🔥 THESE LINES GO HERE (and ONLY here)
        user.setPasswordHash(null);
        user.setProvider(AuthProvider.GOOGLE);

        user.setRole(Role.ROLE_USER);

        return userRepository.save(user);
    }
}




