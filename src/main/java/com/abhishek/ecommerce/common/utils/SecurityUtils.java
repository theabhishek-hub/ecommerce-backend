package com.abhishek.ecommerce.common.utils;

import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

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
        
        // Check seller status directly from User entity (source of truth)
        try {
            var user = userRepository.findById(userId).orElse(null);
            return user != null && SellerStatus.APPROVED.equals(user.getSellerStatus());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh the user's Spring Security principal after status changes.
     * This is called after user updates (like seller approval) to immediately
     * reflect changes in the current session.
     * 
     * @param userId The user ID whose principal should be refreshed
     */
    public void refreshUserPrincipal(Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            
            String email = user.getEmail();
            var userDetails = userDetailsService.loadUserByUsername(email);
            
            // Create new authentication token with updated user details
            UsernamePasswordAuthenticationToken newAuth = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    userDetails.getAuthorities()
                );
            
            // Update the security context
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            
        } catch (Exception e) {
            // Log but don't throw - principal refresh is non-critical
            // User can just log out and log back in if needed
        }
    }
}

