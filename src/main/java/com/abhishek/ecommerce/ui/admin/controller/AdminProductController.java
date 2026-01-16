package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Product Oversight Controller
 * ROLE_ADMIN only - enforced by SecurityConfig
 * View-only access to all products with enable/disable capability
 */
@Slf4j
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    /**
     * List all products with inventory info
     */
    @GetMapping
    public String productsList(Model model) {
        try {
            List<ProductResponseDto> products = productService.getAllProducts();

            model.addAttribute("title", "Product Oversight");
            model.addAttribute("products", products);
            model.addAttribute("hasProducts", !products.isEmpty());

            log.info("Admin loaded products list - Total: {}", products.size());
            return "admin/products/list";
        } catch (Exception e) {
            log.error("Error loading products list", e);
            model.addAttribute("errorMessage", "Unable to load products. Please try again.");
            return "admin/products/list";
        }
    }

    /**
     * View product details
     */
    @GetMapping("/{productId}")
    public String productDetails(@PathVariable Long productId, Model model) {
        try {
            ProductResponseDto product = productService.getProductById(productId);

            model.addAttribute("title", "Product Details");
            model.addAttribute("product", product);

            log.info("Admin viewed product details: {}", productId);
            return "admin/products/details";
        } catch (Exception e) {
            log.error("Error loading product details: {}", productId, e);
            model.addAttribute("errorMessage", "Product not found.");
            return "redirect:/admin/products?error=not_found";
        }
    }

    /**
     * Enable/Disable product
     */
    @PostMapping("/{productId}/toggle")
    public String toggleProductStatus(@PathVariable Long productId) {
        try {
            ProductResponseDto product = productService.getProductById(productId);
            
            // Toggle product status
            if ("ACTIVE".equals(product.getStatus())) {
                productService.deactivateProduct(productId);
                log.info("Admin deactivated product: {}", productId);
            } else {
                productService.activateProduct(productId);
                log.info("Admin activated product: {}", productId);
            }

            return "redirect:/admin/products?success=status_updated";
        } catch (Exception e) {
            log.error("Error toggling product status: {}", productId, e);
            return "redirect:/admin/products?error=toggle_failed";
        }
    }
}
