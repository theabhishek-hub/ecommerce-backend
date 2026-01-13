 package com.abhishek.ecommerce.config.appProperties;

import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config.security")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityProperties {

    @Positive
    private int maxFailedAttempts;

    @Positive
    private int lockoutDurationMinutes;

    @Positive
    private int passwordResetTokenExpiryHours;

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public int getPasswordResetTokenExpiryHours() {
        return passwordResetTokenExpiryHours;
    }

    public void setPasswordResetTokenExpiryHours(int passwordResetTokenExpiryHours) {
        this.passwordResetTokenExpiryHours = passwordResetTokenExpiryHours;
    }
}

