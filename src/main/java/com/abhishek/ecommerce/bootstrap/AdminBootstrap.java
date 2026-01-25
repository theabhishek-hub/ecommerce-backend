package com.abhishek.ecommerce.bootstrap;

import com.abhishek.ecommerce.config.appProperties.AdminProperties;
import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.abhishek.ecommerce.shared.enums.AuthProvider;
import com.abhishek.ecommerce.user.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Bootstrap component to create admin user on application startup
 * 
 * Configuration Flow:
 * 1. Environment variables (ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_FULL_NAME)
 * 2. YAML configuration (application-dev.yml, application-prod.yml)
 * 3. AdminProperties class (config.admin prefix)
 * 4. Injected into AdminBootstrap for user creation
 * 
 * Only runs on non-test profiles (dev, prod)
 * Skips creation if admin email already exists
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile({"dev", "!test"})
public class AdminBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        createAdminIfNotExists();
    }

    /**
     * Create admin user from configuration properties if not already exists
     * 
     * Credentials are loaded from:
     * - config.admin.email (from ADMIN_EMAIL env var)
     * - config.admin.password (from ADMIN_PASSWORD env var)
     * - config.admin.full-name (from ADMIN_FULL_NAME env var)
     */
    private void createAdminIfNotExists() {
        try {
            // Validate admin properties are configured
            if (!adminProperties.isConfigured()) {
                log.warn("Admin properties not fully configured, skipping bootstrap");
                return;
            }

            String adminEmail = adminProperties.getEmail();

            // Check if admin already exists
            if (userRepository.existsByEmail(adminEmail)) {
                log.info("Admin already exists with email: {}, skipping bootstrap", adminEmail);
                return;
            }

            // Create new admin user
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminProperties.getPassword()));
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRoles(Set.of(Role.ROLE_ADMIN));
            admin.setProvider(AuthProvider.LOCAL);
            admin.setFullName(adminProperties.getFullName());

            userRepository.save(admin);

            log.warn("ADMIN USER BOOTSTRAPPED -> email: {}, fullName: {}", 
                    adminEmail, adminProperties.getFullName());
            log.info("Admin user created successfully during application startup");
        } catch (Exception e) {
            log.error("Failed to create admin user during bootstrap: {}", e.getMessage(), e);
            // Don't fail the application startup if admin creation fails
        }
    }
}
