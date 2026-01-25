package com.abhishek.ecommerce.config.appProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Admin credentials configuration properties
 * Binds from environment variables via application.yml:
 * config.admin.email -> ${ADMIN_EMAIL}
 * config.admin.password -> ${ADMIN_PASSWORD}
 * config.admin.full-name -> ${ADMIN_FULL_NAME}
 * 
 * Flow: ENV -> YAML -> Properties -> AdminBootstrap
 * 
 * Example environment variables:
 * ADMIN_EMAIL=admin@example.com
 * ADMIN_PASSWORD=SecurePassword123!
 * ADMIN_FULL_NAME=System Administrator
 */
@Component
@ConfigurationProperties(prefix = "config.admin")
@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminProperties {

    @NotBlank(message = "Admin email cannot be blank")
    @Email(message = "Admin email must be a valid email address")
    private String email;

    @NotBlank(message = "Admin password cannot be blank")
    private String password;

    @NotBlank(message = "Admin full name cannot be blank")
    private String fullName;

    /**
     * Check if admin credentials are configured
     */
    public boolean isConfigured() {
        return email != null && !email.isBlank() &&
               password != null && !password.isBlank() &&
               fullName != null && !fullName.isBlank();
    }
}
