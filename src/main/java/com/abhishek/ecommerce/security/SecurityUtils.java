package com.abhishek.ecommerce.security;

import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName();
    }

    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username == null) return null;
        User user = userRepository.findByEmail(username).orElse(null);
        return user != null ? user.getId() : null;
    }

    public boolean isUserId(Long userId) {
        Long current = getCurrentUserId();
        return current != null && current.equals(userId);
    }
}

