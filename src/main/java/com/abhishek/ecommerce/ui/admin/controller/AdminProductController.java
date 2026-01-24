package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.product.service.CategoryService;
import com.abhishek.ecommerce.product.service.BrandService;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final InventoryService inventoryService;

    /**
     * List all products with inventory info
     */
    @GetMapping
    public String productsList(Model model) {
        try {
            List<ProductResponseDto> products = productService.getAllProducts();
            
            // Fetch inventory information for each product
            products.forEach(product -> {
                try {
                    InventoryResponseDto inventory = inventoryService.getAvailableStock(product.getId());
                    if (inventory != null) {
                        product.setQuantity(inventory.getQuantity());
                    }
                } catch (Exception e) {
                    log.debug("No inventory found for product: {}", product.getId());
                }
            });

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
            
            // Fetch inventory information
            try {
                InventoryResponseDto inventory = inventoryService.getAvailableStock(productId);
                if (inventory != null) {
                    product.setQuantity(inventory.getQuantity());
                }
            } catch (Exception e) {
                log.debug("No inventory found for product: {}", productId);
            }

            model.addAttribute("title", "Product Details");
            model.addAttribute("product", product);

            log.info("Admin viewed product details: {}", productId);
            return "admin/products/details";
        } catch (Exception e) {
            log.error("Error loading product details: {}", productId, e);
            model.addAttribute("title", "Product Details");
            model.addAttribute("errorMessage", "Product not found.");
            return "admin/products/details";
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

    /**
     * GET /admin/products/add
     * Show add product form
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        try {
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            List<BrandResponseDto> brands = brandService.getAllBrands();
            
            model.addAttribute("title", "Add New Product");
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("product", new ProductCreateRequestDto());
            
            log.info("Admin add product form displayed");
            return "admin/products/add";
        } catch (Exception e) {
            log.error("Error loading add product form", e);
            model.addAttribute("error", "Unable to load product form");
            return "admin/products/add";
        }
    }

    /**
     * POST /admin/products
     * Create a new product
     */
    @PostMapping
    public String createProduct(@Valid @ModelAttribute ProductCreateRequestDto productDto, 
                               Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductResponseDto createdProduct = productService.createProduct(productDto);
            
            log.info("Product created successfully by admin. ProductId={}", createdProduct.getId());
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            log.error("Error creating product", e);
            model.addAttribute("error", "Unable to create product: " + e.getMessage());
            model.addAttribute("title", "Add New Product");
            model.addAttribute("product", productDto);
            return "admin/products/add";
        }
    }
}
