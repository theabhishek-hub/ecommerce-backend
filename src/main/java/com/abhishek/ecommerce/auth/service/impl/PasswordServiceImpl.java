package com.abhishek.ecommerce.auth.service.impl;

import com.abhishek.ecommerce.auth.service.PasswordService;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import com.abhishek.ecommerce.config.appProperties.SecurityProperties;
import com.abhishek.ecommerce.security.events.SecurityEventLogger;
import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.abhishek.ecommerce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final RefreshTokenService refreshTokenService;
    private final SecurityEventLogger securityEventLogger;
    private final SecurityProperties securityProperties;
    private final NotificationService notificationService;

    // In-memory store for reset tokens (in production, use Redis or database)
    // For now, we'll use a simple approach - in production, create a PasswordResetToken entity
    private final java.util.Map<String, PasswordResetTokenInfo> resetTokens = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        String username = securityUtils.getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("User not authenticated");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Invalidate all refresh tokens (force re-login)
        refreshTokenService.deleteByUsername(username);

        String ipAddress = getClientIpAddress();
        securityEventLogger.logPasswordChange(username, ipAddress);

        log.info("Password changed successfully for user: {}", username);
    }

    @Override
    public void requestPasswordReset(String email) {
        // Load fresh config as a local variable to avoid storing as a field
        int resetTokenExpiryHours = securityProperties.getPasswordResetTokenExpiryHours();

        User user = userRepository.findByEmail(email).orElse(null);
        
        // Don't reveal if user exists (security best practice)
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(resetTokenExpiryHours);
        
        resetTokens.put(resetToken, new PasswordResetTokenInfo(email, expiry));

        String ipAddress = getClientIpAddress();
        securityEventLogger.logPasswordResetRequest(email, ipAddress);

        // Send password reset email (async side effect)
        notificationService.sendPasswordResetEmail(email, resetToken);

        // In production, send email with reset link
        // For now, just log it
        log.info("Password reset token generated for user: {} | Token: {} | Expires: {}", 
                email, resetToken, expiry);
        // TODO: Send email with reset link: /api/auth/password/reset?token={resetToken}
    }

    @Override
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        PasswordResetTokenInfo tokenInfo = resetTokens.get(resetToken);
        
        if (tokenInfo == null) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        if (tokenInfo.expiry.isBefore(LocalDateTime.now())) {
            resetTokens.remove(resetToken);
            throw new IllegalArgumentException("Reset token has expired");
        }

        User user = userRepository.findByEmail(tokenInfo.email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Remove used token
        resetTokens.remove(resetToken);

        // Invalidate all refresh tokens
        refreshTokenService.deleteByUsername(user.getEmail());

        String ipAddress = getClientIpAddress();
        securityEventLogger.logPasswordResetSuccess(user.getEmail(), ipAddress);

        log.info("Password reset successfully for user: {}", user.getEmail());
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

    private static class PasswordResetTokenInfo {
        final String email;
        final LocalDateTime expiry;

        PasswordResetTokenInfo(String email, LocalDateTime expiry) {
            this.email = email;
            this.expiry = expiry;
        }
    }
}

