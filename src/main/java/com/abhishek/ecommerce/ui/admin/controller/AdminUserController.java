package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.shared.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin User Management Controller
 * ROLE_ADMIN only - enforced by SecurityConfig
 * Promotes/Demotes users between USER and SELLER roles
 * NEVER modifies ADMIN accounts
 */
@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * List all users
     */
    @GetMapping
    public String usersList(Model model) {
        try {
            List<UserResponseDto> users = userService.getAllUsers();

            model.addAttribute("title", "User Management");
            model.addAttribute("users", users);
            model.addAttribute("hasUsers", !users.isEmpty());

            log.info("Admin loaded users list - Total: {}", users.size());
            return "admin/users/list";
        } catch (Exception e) {
            log.error("Error loading users list", e);
            model.addAttribute("title", "User Management");
            model.addAttribute("errorMessage", "Unable to load users. Please try again.");
            model.addAttribute("hasUsers", false);
            model.addAttribute("users", new java.util.ArrayList<>());
            return "admin/users/list";
        }
    }

    /**
     * Promote user to SELLER
     * NOTE: Deprecated - Use SellerService.approveSeller() instead
     * Only allows USER role to be promoted to SELLER
     */
    @PostMapping("/{userId}/promote")
    @Deprecated
    public String promoteToSeller(@PathVariable Long userId, Model model) {
        log.warn("Deprecated endpoint /admin/users/{}/promote called", userId);
        model.addAttribute("errorMessage", "Use seller management endpoints instead.");
        return "redirect:/admin/users?error=endpoint_deprecated";
    }

    /**
     * Demote SELLER to USER
     * NOTE: Deprecated - Use SellerService.suspendSeller() instead
     * Only allows SELLER role to be demoted to USER
     * NEVER demotes ADMIN
     */
    @PostMapping("/{userId}/demote")
    @Deprecated
    public String demoteToUser(@PathVariable Long userId, Model model) {
        log.warn("Deprecated endpoint /admin/users/{}/demote called", userId);
        model.addAttribute("errorMessage", "Use seller management endpoints instead.");
        return "redirect:/admin/users?error=endpoint_deprecated";
    }
}
