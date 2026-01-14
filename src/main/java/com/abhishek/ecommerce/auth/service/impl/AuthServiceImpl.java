package com.abhishek.ecommerce.auth.service.impl;

import com.abhishek.ecommerce.auth.dto.AuthResponseDto;
import com.abhishek.ecommerce.auth.dto.LoginRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupResponseDto;
import com.abhishek.ecommerce.auth.service.AuthService;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import com.abhishek.ecommerce.config.appProperties.SecurityProperties;
import com.abhishek.ecommerce.config.security.JwtUtil;
import com.abhishek.ecommerce.config.security.SecurityEventLogger;
import com.abhishek.ecommerce.user.entity.AuthProvider;
import com.abhishek.ecommerce.user.entity.Role;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.UserStatus;
import com.abhishek.ecommerce.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final SecurityEventLogger securityEventLogger;
    private final SecurityProperties securityProperties;

    private int maxFailedAttempts;
    private int lockoutDurationMinutes;

    @Override
    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {

        log.info("Signup attempt for email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setStatus(UserStatus.ACTIVE);
        user.setProvider(AuthProvider.LOCAL);

        user = userRepository.save(user);

        // Return user info only (no tokens on signup)
        return SignupResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role("USER") // Signup always assigns ROLE_USER
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        // Load config values from SecurityProperties at method entry so changes to env will be picked up each call
        this.maxFailedAttempts = securityProperties.getMaxFailedAttempts();
        this.lockoutDurationMinutes = securityProperties.getLockoutDurationMinutes();

        String ipAddress = getClientIpAddress();
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        try {
            // Check if account is locked
            if (user != null && isAccountLocked(user)) {
                securityEventLogger.logLoginFailure(request.getEmail(), ipAddress, "Account is locked");
                throw new IllegalStateException("Account is locked. Please try again later.");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Authentication successful - reset failed attempts
            if (user != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }

            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            if (user.getStatus() != UserStatus.ACTIVE) {
                securityEventLogger.logLoginFailure(request.getEmail(), ipAddress, "User account is not active");
                throw new IllegalStateException("User is not active");
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getRoles().stream().map(Role::name).toList());
            com.abhishek.ecommerce.auth.entity.RefreshToken refreshTokenEntity = refreshTokenService.createOrReplaceRefreshToken(user);
            String refreshToken = refreshTokenEntity.getToken();

            // Log successful login
            securityEventLogger.logLoginSuccess(user.getEmail(), ipAddress);

            return AuthResponseDto.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream().map(r -> r.name().replace("ROLE_", "")).collect(java.util.stream.Collectors.toSet())) // Convert ROLE_USER to USER
                    .tokenType("Bearer")
                    .refreshToken(refreshToken)
                    .refreshTokenExpiryMs(refreshTokenEntity.getExpiresAt().toEpochMilli())
                    .build();

        } catch (AuthenticationException ex) {
            // Authentication failed - increment failed attempts
            if (user != null) {
                handleFailedLogin(user, ipAddress);
            } else {
                securityEventLogger.logLoginFailure(request.getEmail(), ipAddress, "Invalid credentials");
            }
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    private void handleFailedLogin(User user, String ipAddress) {
        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            securityEventLogger.logAccountLockout(user.getEmail(), ipAddress, attempts);
        } else {
            securityEventLogger.logLoginFailure(user.getEmail(), ipAddress, 
                    "Invalid credentials. Attempts: " + attempts + "/" + maxFailedAttempts);
        }

        userRepository.save(user);
    }

    private boolean isAccountLocked(User user) {
        if (user.getLockedUntil() == null) {
            return false;
        }
        if (user.getLockedUntil().isAfter(LocalDateTime.now())) {
            return true;
        }
        // Lock expired - reset
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        return false;
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Could not get client IP address", e);
        }
        return "unknown";
    }
}
