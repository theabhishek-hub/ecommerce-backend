package com.abhishek.ecommerce.security.filter;

import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to refresh SecurityContext for approved sellers who don't have ROLE_SELLER in their session.
 * 
 * This handles the case where:
 * - User was approved by admin (status = APPROVED, has ROLE_SELLER in DB)
 * - But their session still has old roles (no ROLE_SELLER)
 * - They try to access /seller/** routes and get 403
 * 
 * This filter runs BEFORE Spring Security checks roles, so it can refresh the session
 * and allow access to seller routes.
 */
@Slf4j
@Component
@Order(1) // Run early, before Spring Security's role checks
@RequiredArgsConstructor
public class SellerRoleRefreshFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Only process seller routes (except /seller/apply which doesn't require ROLE_SELLER)
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/seller/") && !path.equals("/seller/apply")) {
            
            // Check if user is authenticated
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
                
                Long userId = securityUtils.getCurrentUserId();
                if (userId != null) {
                    try {
                        // Check if user is approved seller in database
                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null && SellerStatus.APPROVED.equals(user.getSellerStatus())) {
                            
                            // Check if user has ROLE_SELLER in their current session
                            boolean hasSellerRole = auth.getAuthorities() != null &&
                                    auth.getAuthorities().stream()
                                            .anyMatch(authority -> "ROLE_SELLER".equals(authority.getAuthority()));
                            
                            // If user is approved but doesn't have ROLE_SELLER in session, refresh it
                            if (!hasSellerRole) {
                                log.info("🔄 Refreshing SecurityContext for approved seller {} (missing ROLE_SELLER in session)", userId);
                                log.info("🔄 Current authorities: {}", auth.getAuthorities());
                                securityUtils.refreshUserPrincipal(userId);
                                log.info("✅ SecurityContext refreshed for user {}", userId);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Could not refresh SecurityContext in filter for user {}", userId, e);
                        // Continue with filter chain even if refresh fails
                    }
                }
            }
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
