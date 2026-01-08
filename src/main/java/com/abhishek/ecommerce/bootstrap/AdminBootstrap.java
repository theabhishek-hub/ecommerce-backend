package com.abhishek.ecommerce.bootstrap;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.Role;
import com.abhishek.ecommerce.user.entity.UserStatus;
import com.abhishek.ecommerce.user.entity.AuthProvider;
import com.abhishek.ecommerce.user.repository.UserRepository;

import org.springframework.context.annotation.Profile;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class AdminBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Runs ONCE at application startup.
     * Creates initial ROLE_ADMIN users if none exist.
     */
    @PostConstruct
    public void initAdmins() {

        // If at least one admin already exists, do nothing
        if (userRepository.existsByRole(Role.ROLE_ADMIN)) {
            return;
        }

        User admin1 = new User();
        admin1.setEmail("admin1@test.com");
        admin1.setPasswordHash(passwordEncoder.encode("admin123"));
        admin1.setRole(Role.ROLE_ADMIN);
        admin1.setStatus(UserStatus.ACTIVE);
        admin1.setProvider(AuthProvider.LOCAL);
        admin1.setFailedLoginAttempts(0);
        admin1.setLockedUntil(null);

        User admin2 = new User();
        admin2.setEmail("admin2@test.com");
        admin2.setPasswordHash(passwordEncoder.encode("admin123"));
        admin2.setRole(Role.ROLE_ADMIN);
        admin2.setStatus(UserStatus.ACTIVE);
        admin2.setProvider(AuthProvider.LOCAL);
        admin2.setFailedLoginAttempts(0);
        admin2.setLockedUntil(null);

        userRepository.saveAll(List.of(admin1, admin2));

        log.warn("AdminBootstrap: initial ROLE_ADMIN users created");
    }
}


