package com.abhishek.ecommerce.auth.controller;

import com.abhishek.ecommerce.auth.dto.RefreshRequestDto;
import com.abhishek.ecommerce.auth.dto.AuthResponseDto;
import com.abhishek.ecommerce.auth.entity.RefreshToken;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import com.abhishek.ecommerce.config.security.JwtUtil;
import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class RefreshController {

    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(@RequestBody RefreshRequestDto request) {
        String provided = request.getRefreshToken();
        RefreshToken rt = refreshTokenService.validateAndGet(provided);

        String username = rt.getUsername();
        
        // Get user to retrieve role and userId
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String newAccess = jwtUtil.generateToken(username, user.getRole().name());

        // replace persisted refresh token
        RefreshToken newRefreshTokenEntity = refreshTokenService.createOrReplaceRefreshToken(user);
        String newRefresh = newRefreshTokenEntity.getToken();

        AuthResponseDto dto = AuthResponseDto.builder()
                .token(newAccess)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name().replace("ROLE_", "")) // Convert ROLE_USER to USER
                .refreshToken(newRefresh)
                .refreshTokenExpiryMs(newRefreshTokenEntity.getExpiresAt().toEpochMilli())
                .tokenType("Bearer")
                .build();

        return ResponseEntity.ok(ApiResponseBuilder.success("Token refreshed", dto));
    }
}
