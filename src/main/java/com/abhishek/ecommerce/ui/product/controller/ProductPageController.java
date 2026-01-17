package com.abhishek.ecommerce.ui.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * UI Controller for product catalog views (Thymeleaf).
 * Serves HTML pages that fetch product data from REST APIs using JavaScript.
 * 
 * This is a view-only controller - no business logic or database operations.
 * All product data is fetched client-side from /api/v1/products endpoints.
 */
@Controller
public class ProductPageController {

    /**
     * Display product catalog listing page.
     * Loads the list view; products are fetched via JavaScript from REST API.
     */
    @GetMapping({"/products", "/products-page"})
    public String listProducts(Model model) {
        model.addAttribute("title", "Products Catalog");
        return "product/list";
    }

    /**
     * Display product details page for a specific product.
     * Loads the details view; product data is fetched via JavaScript from REST API.
     * 
     * @param id the product ID
     * @param model the model to pass attributes to the view
     * @return the product details template
     */
    @GetMapping({"/products/{id}", "/products-page/{id}"})
    public String productDetails(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Product Details");
        model.addAttribute("productId", id);
        return "product/details";
    }
}
