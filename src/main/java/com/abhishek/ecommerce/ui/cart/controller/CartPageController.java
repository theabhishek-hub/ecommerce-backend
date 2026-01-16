package com.abhishek.ecommerce.ui.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * UI Controller for shopping cart view (Thymeleaf).
 * Serves HTML page that manages cart using JavaScript and localStorage.
 * 
 * This is a view-only controller - no business logic or database operations.
 * All cart state is managed client-side via localStorage.
 * Cart data is formatted for API consumption when needed.
 */
@Controller
@RequestMapping("/cart")
public class CartPageController {

    /**
     * Display shopping cart page.
     * Cart items are managed entirely via JavaScript and localStorage.
     */
    @GetMapping
    public String cart(Model model) {
        model.addAttribute("title", "Shopping Cart");
        return "cart/cart";
    }
}
