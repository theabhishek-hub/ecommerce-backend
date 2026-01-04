package com.abhishek.ecommerce.auth.controller;

import com.abhishek.ecommerce.auth.dto.ApiResponseWrapper;
import com.abhishek.ecommerce.auth.dto.RefreshRequestDto;
import com.abhishek.ecommerce.auth.dto.AuthResponseDto;
import com.abhishek.ecommerce.auth.entity.RefreshToken;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import com.abhishek.ecommerce.config.security.JwtUtil;
import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class RefreshController {

    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(@RequestBody RefreshRequestDto request) {
        String provided = request.getRefreshToken();
        RefreshToken rt = refreshTokenService.validateAndGet(provided);

        String username = rt.getUsername();
        String newAccess = jwtUtil.generateToken(username);
        String newRefresh = jwtUtil.generateRefreshToken(username);

        // replace persisted refresh token
        long expiresMs = Long.parseLong(System.getProperty("app.jwt.refresh-expiration-ms", "604800000"));
        refreshTokenService.createOrReplaceRefreshToken(username, expiresMs, newRefresh);

        AuthResponseDto dto = AuthResponseDto.builder()
                .token(newAccess)
                .refreshToken(newRefresh)
                .refreshTokenExpiryMs(Instant.now().plusMillis(expiresMs).toEpochMilli())
                .tokenType("Bearer")
                .build();

        return ResponseEntity.ok(ApiResponseBuilder.success("Token refreshed", dto));
    }
}

