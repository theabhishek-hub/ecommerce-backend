package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin User Management Controller
 * ROLE_ADMIN only - enforced by SecurityConfig
 * Full CRUD operations for user management with search, filter, and sort
 */
@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * List all users with search, filter, and sort
     */
    @GetMapping
    public String usersList(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false, defaultValue = "email") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            Model model) {
        try {
            List<UserResponseDto> users = userService.getAllUsers();
            
            // Search by email or name
            if (q != null && !q.isEmpty()) {
                String query = q.toLowerCase();
                users = users.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(query) || 
                               u.getFullName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            }
            
            // Filter by status
            if (status != null && !status.isEmpty()) {
                try {
                    UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
                    users = users.stream()
                        .filter(u -> u.getStatus() != null && u.getStatus().equals(userStatus))
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter: {}", status);
                }
            }
            
            // Filter by role
            if (role != null && !role.isEmpty()) {
                users = users.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().contains(role))
                    .collect(Collectors.toList());
            }
            
            // Sort
            users = sortUsers(users, sortBy, sortOrder);

            model.addAttribute("title", "User Management");
            model.addAttribute("users", users);
            model.addAttribute("hasUsers", !users.isEmpty());
            model.addAttribute("searchQuery", q);
            model.addAttribute("filterStatus", status);
            model.addAttribute("filterRole", role);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortOrder", sortOrder);

            log.info("Admin loaded users list - Total: {}, Search: {}, Status: {}, Role: {}", 
                    users.size(), q, status, role);
            return "admin/users/list";
        } catch (Exception e) {
            log.error("Error loading users list", e);
            model.addAttribute("title", "User Management");
            model.addAttribute("errorMessage", "Unable to load users. Please try again.");
            model.addAttribute("hasUsers", false);
            model.addAttribute("users", new ArrayList<>());
            return "admin/users/list";
        }
    }

    /**
     * View user details
     */
    @GetMapping("/{userId}")
    public String viewUser(@PathVariable Long userId, Model model) {
        try {
            UserResponseDto user = userService.getUserById(userId);
            model.addAttribute("title", "User Details");
            model.addAttribute("user", user);
            return "admin/users/view";
        } catch (Exception e) {
            log.error("Error loading user details for userId: {}", userId, e);
            return "redirect:/admin/users?error=not_found";
        }
    }

    /**
     * Show manage roles form
     */
    @GetMapping("/{userId}/roles")
    public String manageRoles(@PathVariable Long userId, Model model) {
        try {
            UserResponseDto user = userService.getUserById(userId);
            model.addAttribute("title", "Manage Roles");
            model.addAttribute("user", user);
            return "admin/users/roles";
        } catch (Exception e) {
            log.error("Error loading user for role management userId: {}", userId, e);
            return "redirect:/admin/users?error=not_found";
        }
    }

    /**
     * Assign seller role
     */
    @PostMapping("/{userId}/roles/seller/assign")
    public String assignSellerRole(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.assignSellerRole(userId);
            redirectAttributes.addFlashAttribute("success", "Seller role assigned successfully");
            return "redirect:/admin/users/" + userId + "/roles";
        } catch (Exception e) {
            log.error("Error assigning seller role userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to assign seller role: " + e.getMessage());
            return "redirect:/admin/users/" + userId + "/roles";
        }
    }

    /**
     * Remove seller role
     */
    @PostMapping("/{userId}/roles/seller/remove")
    public String removeSellerRole(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.removeSellerRole(userId);
            redirectAttributes.addFlashAttribute("success", "Seller role removed successfully");
            return "redirect:/admin/users/" + userId + "/roles";
        } catch (Exception e) {
            log.error("Error removing seller role userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to remove seller role: " + e.getMessage());
            return "redirect:/admin/users/" + userId + "/roles";
        }
    }

    /**
     * Activate user
     */
    @PostMapping("/{userId}/activate")
    public String activateUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.activateUser(userId);
            redirectAttributes.addFlashAttribute("success", "User activated successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            log.error("Error activating user userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to activate user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Deactivate user
     */
    @PostMapping("/{userId}/deactivate")
    public String deactivateUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deactivateUser(userId);
            redirectAttributes.addFlashAttribute("success", "User deactivated successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            log.error("Error deactivating user userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to deactivate user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Delete user (soft delete)
     */
    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            log.error("Error deleting user userId: {}", userId, e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Helper method to sort users
     */
    private List<UserResponseDto> sortUsers(List<UserResponseDto> users, String sortBy, String sortOrder) {
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);

        switch (sortBy.toLowerCase()) {
            case "email":
                return users.stream()
                    .sorted((u1, u2) -> ascending ? u1.getEmail().compareTo(u2.getEmail()) : u2.getEmail().compareTo(u1.getEmail()))
                    .collect(Collectors.toList());
            case "name":
                return users.stream()
                    .sorted((u1, u2) -> ascending ? u1.getFullName().compareTo(u2.getFullName()) : u2.getFullName().compareTo(u1.getFullName()))
                    .collect(Collectors.toList());
            case "status":
                return users.stream()
                    .sorted((u1, u2) -> ascending ? u1.getStatus().compareTo(u2.getStatus()) : u2.getStatus().compareTo(u1.getStatus()))
                    .collect(Collectors.toList());
            case "created":
                return users.stream()
                    .sorted((u1, u2) -> ascending ? u1.getCreatedAt().compareTo(u2.getCreatedAt()) : u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                    .collect(Collectors.toList());
            default:
                return users;
        }
    }
}
