package com.abhishek.ecommerce.ui.cart.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UI Controller for shopping cart view (Thymeleaf).
 * Serves HTML page that manages cart using JavaScript and localStorage.
 * 
 * Access control:
 * - ROLE_USER: Can access (/cart)
 * - ROLE_SELLER: Redirected to /seller/dashboard (sellers cannot shop)
 * - ROLE_ADMIN: Redirected to /admin/dashboard (admins cannot shop)
 * - Anonymous: Can view but cannot checkout
 * 
 * This is a view-only controller - no business logic or database operations.
 * All cart state is managed client-side via localStorage.
 * Cart data is formatted for API consumption when needed.
 */
@Slf4j
@Controller
@RequestMapping("/cart")
public class CartPageController {

    /**
     * Display shopping cart page.
     * Cart items are managed entirely via JavaScript and localStorage.
     * 
     * ADMIN/SELLER are redirected to their respective dashboards.
     */
    @GetMapping
    public String cart(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        // Check if authenticated user is ADMIN or SELLER
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isSeller = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_SELLER"));

            if (isAdmin) {
                log.warn("ADMIN user attempting to access cart, redirecting to admin dashboard");
                redirectAttributes.addFlashAttribute("info", "Admins cannot access cart");
                return "redirect:/admin";
            }
            if (isSeller) {
                log.warn("SELLER user attempting to access cart, redirecting to seller dashboard");
                redirectAttributes.addFlashAttribute("info", "Sellers cannot access cart");
                return "redirect:/seller/dashboard";
            }
        }

        model.addAttribute("title", "Shopping Cart");
        return "cart/cart";
    }
}
