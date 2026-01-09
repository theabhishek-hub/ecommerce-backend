package com.abhishek.ecommerce.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized security event logging
 */
@Slf4j
@Component
public class SecurityEventLogger {

    private static final String SECURITY_LOG_PREFIX = "[SECURITY]";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Log authentication success
     */
    @Async("taskExecutor")
    public void logLoginSuccess(String username, String ipAddress) {
        log.warn("{} {} LOGIN_SUCCESS | User: {} | IP: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), username, ipAddress);
    }

    /**
     * Log authentication failure
     */
    @Async("taskExecutor")
    public void logLoginFailure(String username, String ipAddress, String reason) {
        log.warn("{} {} LOGIN_FAILURE | User: {} | IP: {} | Reason: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), username, ipAddress, reason);
    }

    /**
     * Log unauthorized access attempt (401)
     */
    @Async("taskExecutor")
    public void logUnauthorizedAccess(HttpServletRequest request, String reason) {
        String ipAddress = getClientIpAddress(request);
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        log.warn("{} {} UNAUTHORIZED_ACCESS | IP: {} | Method: {} | Path: {} | Reason: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), ipAddress, method, path, reason);
    }

    /**
     * Log forbidden access attempt (403)
     */
    @Async("taskExecutor")
    public void logForbiddenAccess(HttpServletRequest request, String reason) {
        String ipAddress = getClientIpAddress(request);
        String path = request.getRequestURI();
        String method = request.getMethod();
        String username = getCurrentUsername();
        
        log.warn("{} {} FORBIDDEN_ACCESS | User: {} | IP: {} | Method: {} | Path: {} | Reason: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), username != null ? username : "anonymous", 
                ipAddress, method, path, reason);
    }

    /**
     * Log token validation failure
     */
    @Async("taskExecutor")
    public void logTokenValidationFailure(String token, String reason, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        log.warn("{} {} TOKEN_VALIDATION_FAILURE | IP: {} | Reason: {} | Path: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), ipAddress, reason, request.getRequestURI());
    }

    /**
     * Log token misuse attempt
     */
    @Async("taskExecutor")
    public void logTokenMisuse(String username, String ipAddress, String reason) {
        log.warn("{} {} TOKEN_MISUSE | User: {} | IP: {} | Reason: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), username, ipAddress, reason);
    }

    /**
     * Log account lockout
     */
    @Async("taskExecutor")
    public void logAccountLockout(String username, String ipAddress, int failedAttempts) {
        log.error("{} {} ACCOUNT_LOCKOUT | User: {} | IP: {} | Failed Attempts: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), username, ipAddress, failedAttempts);
    }

    /**
     * Log password change
     */
    @Async("taskExecutor")
    public void logPasswordChange(String username, String ipAddress) {
        log.info("{} {} PASSWORD_CHANGE | User: {} | IP: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), username, ipAddress);
    }

    /**
     * Log password reset request
     */
    @Async("taskExecutor")
    public void logPasswordResetRequest(String username, String ipAddress) {
        log.info("{} {} PASSWORD_RESET_REQUEST | User: {} | IP: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), username, ipAddress);
    }

    /**
     * Log password reset success
     */
    @Async("taskExecutor")
    public void logPasswordResetSuccess(String username, String ipAddress) {
        log.info("{} {} PASSWORD_RESET_SUCCESS | User: {} | IP: {}", 
                SECURITY_LOG_PREFIX, getTimestamp(), username, ipAddress);
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }

    private String getClientIpAddress(HttpServletRequest request) {
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

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return null;
    }
}










