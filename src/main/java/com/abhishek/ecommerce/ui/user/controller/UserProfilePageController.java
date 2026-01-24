package com.abhishek.ecommerce.ui.user.controller;

import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UI Controller for user profile and settings pages.
 * Serves HTML pages for authenticated users to manage their profile and preferences.
 * 
 * Access control:
 * - ROLE_USER: Can access all pages
 * - ROLE_SELLER: Can access (SELLER extends USER permissions)
 * - ROLE_ADMIN: Redirected to admin dashboard
 * 
 * This is a view-only controller - renders Thymeleaf templates.
 * Data modifications are handled via API endpoints.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserProfilePageController {

    private final UserService userService;

    /**
     * Display user profile page
     * Shows current user information with option to edit
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        try {
            UserResponseDto currentUser = userService.getCurrentUserProfile();
            model.addAttribute("title", "Profile Information");
            model.addAttribute("user", currentUser);
            log.info("User {} loaded profile page", currentUser.getId());
            return "user/profile";
        } catch (Exception e) {
            log.error("Error loading profile page", e);
            model.addAttribute("errorMessage", "Unable to load profile. Please try again.");
            return "user/profile";
        }
    }

    /**
     * Display saved addresses page
     * Shows list of saved delivery addresses
     */
    @GetMapping("/addresses")
    public String addresses(Model model) {
        try {
            UserResponseDto currentUser = userService.getCurrentUserProfile();
            model.addAttribute("title", "Saved Addresses");
            model.addAttribute("user", currentUser);
            model.addAttribute("addresses", null); // TODO: Get addresses from service
            log.info("User {} loaded addresses page", currentUser.getId());
            return "user/addresses";
        } catch (Exception e) {
            log.error("Error loading addresses page", e);
            model.addAttribute("errorMessage", "Unable to load addresses. Please try again.");
            return "user/addresses";
        }
    }

    /**
     * Display saved payment methods page
     * Shows list of saved payment methods
     */
    @GetMapping("/payments")
    public String payments(Model model) {
        try {
            UserResponseDto currentUser = userService.getCurrentUserProfile();
            model.addAttribute("title", "Payment Methods");
            model.addAttribute("user", currentUser);
            model.addAttribute("payments", null); // TODO: Get payment methods from service
            log.info("User {} loaded payments page", currentUser.getId());
            return "user/payments";
        } catch (Exception e) {
            log.error("Error loading payments page", e);
            model.addAttribute("errorMessage", "Unable to load payment methods. Please try again.");
            return "user/payments";
        }
    }

    /**
     * Display user preferences page
     * Shows notification and other user preferences
     */
    @GetMapping("/preferences")
    public String preferences(Model model) {
        try {
            UserResponseDto currentUser = userService.getCurrentUserProfile();
            model.addAttribute("title", "Preferences");
            model.addAttribute("user", currentUser);
            log.info("User {} loaded preferences page", currentUser.getId());
            return "user/preferences";
        } catch (Exception e) {
            log.error("Error loading preferences page", e);
            model.addAttribute("errorMessage", "Unable to load preferences. Please try again.");
            return "user/preferences";
        }
    }

    /**
     * Display user dashboard (home page after login)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            UserResponseDto currentUser = userService.getCurrentUserProfile();
            model.addAttribute("title", "Dashboard");
            model.addAttribute("username", currentUser.getFullName());
            model.addAttribute("user", currentUser);
            // TODO: Add stats and recent orders
            log.info("User {} loaded dashboard", currentUser.getId());
            return "user/dashboard";
        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("errorMessage", "Unable to load dashboard. Please try again.");
            return "user/dashboard";
        }
    }
}
