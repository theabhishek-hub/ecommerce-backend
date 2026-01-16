package com.abhishek.ecommerce.ui.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * UI Controller for session-based login.
 * Handles Thymeleaf form login flows.
 * Does NOT use JWT - uses Spring Security session cookies.
 * 
 * NOTE: Logout is handled by Spring Security's logout() configuration in SecurityConfig
 * Visiting POST /logout automatically invalidates session and redirects to /login
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    /**
     * Display login form
     * @param model Thymeleaf model
     * @param error error parameter if login failed
     * @return login template
     */
    @GetMapping
    public String loginPage(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("title", "Login");
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        return "auth/login";
    }
}
