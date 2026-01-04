package com.abhishek.ecommerce.config.audit;

import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("springSecurityAuditorAware")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    private final UserRepository userRepository;

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }

        // Get userId instead of username
        String username = authentication.getName();
        if (username == null) {
            return Optional.of("SYSTEM");
        }

        return userRepository.findByEmail(username)
                .map(user -> user.getId().toString())
                .or(() -> Optional.of("SYSTEM"));
    }
}

