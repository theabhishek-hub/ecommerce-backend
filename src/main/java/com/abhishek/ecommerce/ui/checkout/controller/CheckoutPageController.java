package com.abhishek.ecommerce.ui.checkout.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * UI-only controller for checkout page.
 * Pure presentation layer - no service or repository injection.
 * Checkout workflow is client-driven via JavaScript with localStorage cart data.
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutPageController {

    /**
     * Display checkout page.
     * Security check: @authenticated via Spring Security config
     * 
     * @param model Thymeleaf model
     * @return checkout template
     */
    @GetMapping
    public String checkoutPage(Model model) {
        model.addAttribute("title", "Checkout");
        return "checkout/checkout";
    }
}
