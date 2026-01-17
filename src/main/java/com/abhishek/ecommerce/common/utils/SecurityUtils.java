package com.abhishek.ecommerce.common.utils;

import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.seller.repository.SellerRepository;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        
        // Handle OAuth2User separately - extract email from attributes
        if (auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
            String email = oauth2User.getAttribute("email");
            return email != null ? email : auth.getName();
        }
        
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

    /**
     * Check if current user is an approved seller.
     * Fetches fresh seller status from database (source of truth).
     * Used by navbar to conditionally show seller links.
     */
    public boolean isSellerApproved() {
        Long userId = getCurrentUserId();
        if (userId == null) return false;
        
        // Check seller profile status (source of truth)
        return sellerRepository.findByUserId(userId)
                .map(seller -> seller.getStatus() == SellerStatus.APPROVED)
                .orElse(false);
    }
}

