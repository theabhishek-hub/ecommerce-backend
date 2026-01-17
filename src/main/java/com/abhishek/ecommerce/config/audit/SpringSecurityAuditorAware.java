package com.abhishek.ecommerce.config.audit;

import com.abhishek.ecommerce.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("springSecurityAuditorAware")
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

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

        // Disable auto-flush to prevent infinite recursion during @PreUpdate callbacks
        Session session = entityManager.unwrap(Session.class);
        FlushMode previousFlushMode = session.getHibernateFlushMode();
        try {
            session.setHibernateFlushMode(FlushMode.MANUAL);
            return userRepository.findByEmail(username)
                    .map(user -> user.getId().toString())
                    .or(() -> Optional.of("SYSTEM"));
        } finally {
            session.setHibernateFlushMode(previousFlushMode);
        }
    }
}

