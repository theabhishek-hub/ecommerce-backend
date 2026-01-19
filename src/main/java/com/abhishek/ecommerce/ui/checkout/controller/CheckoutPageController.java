package com.abhishek.ecommerce.ui.checkout.controller;

import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UI controller for checkout page and order placement.
 * Handles both page display and order placement via session-based authentication.
 * 
 * Access control:
 * - ROLE_USER: Can access checkout
 * - ROLE_SELLER: Redirected to /seller/dashboard (sellers cannot shop)
 * - ROLE_ADMIN: Redirected to /admin/dashboard (admins cannot shop)
 */
@Slf4j
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutPageController {

    private final OrderService orderService;

    /**
     * Display checkout page.
     * ADMIN/SELLER are redirected to their respective dashboards.
     * 
     * @param model Thymeleaf model
     * @param authentication Spring Security authentication
     * @param redirectAttributes For flash messages
     * @return checkout template or redirect
     */
    @GetMapping
    public String checkoutPage(
            Model model, 
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        // Check if authenticated user is ADMIN or SELLER
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isSeller = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_SELLER"));

            if (isAdmin) {
                log.warn("ADMIN user attempting to access checkout, redirecting to admin dashboard");
                redirectAttributes.addFlashAttribute("info", "Admins cannot access checkout");
                return "redirect:/admin";
            }
            if (isSeller) {
                log.warn("SELLER user attempting to access checkout, redirecting to seller dashboard");
                redirectAttributes.addFlashAttribute("info", "Sellers cannot access checkout");
                return "redirect:/seller/dashboard";
            }
        }

        model.addAttribute("title", "Checkout");
        return "checkout/checkout";
    }

    /**
     * Place order for the current authenticated user.
     * Uses session-based authentication (works with form login, OAuth2, etc.)
     * 
     * @param paymentMethod Payment method (COD or ONLINE) - defaults to COD if not provided
     * @param redirectAttributes For flash messages
     * @param authentication Spring Security authentication
     * @return Redirect to orders page on success
     */
    @PostMapping("/place-order")
    @PreAuthorize("isAuthenticated()")
    public String placeOrder(
            @RequestParam(value = "paymentMethod", required = false, defaultValue = "COD") String paymentMethod,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        
        // Double-check ADMIN/SELLER cannot place orders
        if (authentication != null) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isSeller = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_SELLER"));

            if (isAdmin) {
                log.warn("ADMIN user attempted to place order");
                redirectAttributes.addFlashAttribute("error", "Admins cannot place orders");
                return "redirect:/admin";
            }
            if (isSeller) {
                log.warn("SELLER user attempted to place order");
                redirectAttributes.addFlashAttribute("error", "Sellers cannot place orders");
                return "redirect:/seller/dashboard";
            }
        }

        try {
            // Parse payment method, default to COD for backward compatibility
            com.abhishek.ecommerce.payment.entity.PaymentMethod method;
            try {
                method = com.abhishek.ecommerce.payment.entity.PaymentMethod.valueOf(paymentMethod.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid payment method '{}', defaulting to COD", paymentMethod);
                method = com.abhishek.ecommerce.payment.entity.PaymentMethod.COD;
            }

            OrderResponseDto order = orderService.placeOrderForCurrentUser(method);
            redirectAttributes.addFlashAttribute("success", "Order placed successfully! Order ID: " + order.getId());
            log.info("Order placed successfully via UI: orderId={} paymentMethod={}", order.getId(), method);
            return "redirect:/orders";
        } catch (Exception e) {
            log.error("Error placing order via UI", e);
            redirectAttributes.addFlashAttribute("error", "Failed to place order: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}
