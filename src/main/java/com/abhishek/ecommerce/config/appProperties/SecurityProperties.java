 package com.abhishek.ecommerce.config.appProperties;

import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
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

}

