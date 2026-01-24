package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.product.service.CategoryService;
import com.abhishek.ecommerce.product.service.BrandService;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * UI Controller for seller product management pages.
 * Handles seller's product catalog views and CRUD operations.
 */
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
@Slf4j
public class SellerProductPageController {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final InventoryService inventoryService;

    /**
     * Display seller's product listing page.
     * Only accessible to APPROVED sellers.
     */
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Allow pending sellers with warning flag
        boolean isPending = !SellerStatus.APPROVED.equals(seller.getSellerStatus());

        try {
            // Load all products, filter by seller
            List<ProductResponseDto> allProducts = productService.getAllProducts();
            List<ProductResponseDto> sellerProducts = new java.util.ArrayList<>(allProducts.stream()
                    .filter(product -> product.getSellerId() != null && product.getSellerId().equals(userId))
                    .toList());
            
            // Fetch inventory for each product
            sellerProducts.forEach(product -> {
                try {
                    InventoryResponseDto inventory = inventoryService.getAvailableStock(product.getId());
                    if (inventory != null && inventory.getQuantity() != null) {
                        product.setQuantity(inventory.getQuantity());
                    } else {
                        product.setQuantity(0);
                    }
                } catch (Exception e) {
                    log.debug("No inventory found for product: {}", product.getId());
                    product.setQuantity(0);
                }
            });

            model.addAttribute("title", "My Products");
            model.addAttribute("user", seller);
            model.addAttribute("sellerId", userId);
            model.addAttribute("products", sellerProducts);
            model.addAttribute("hasProducts", !sellerProducts.isEmpty());
            model.addAttribute("totalProducts", sellerProducts.size());
            model.addAttribute("isPending", isPending);
            model.addAttribute("status", seller.getSellerStatus());
            
            log.info("Seller {} loaded products - Total: {}", userId, sellerProducts.size());
            return "seller/products/list";
        } catch (Exception e) {
            log.error("Error loading products for seller {}: {}", userId, e.getMessage(), e);
            model.addAttribute("title", "My Products");
            model.addAttribute("user", seller);
            model.addAttribute("sellerId", userId);
            model.addAttribute("products", java.util.Collections.emptyList());
            model.addAttribute("hasProducts", false);
            model.addAttribute("totalProducts", 0);
            model.addAttribute("isPending", isPending);
            model.addAttribute("status", seller.getSellerStatus());
            model.addAttribute("errorMessage", "Unable to load products. Please try again.");
            return "seller/products/list";
        }
    }

    /**
     * Display product details page with inventory and stock management.
     * Only accessible if seller owns the product.
     */
    @GetMapping("/products/{productId}/view")
    public String viewProductDetails(
            @PathVariable Long productId,
            Model model) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Verify seller owns the product
        if (!productService.isSellerOwner(productId, userId)) {
            model.addAttribute("errorMessage", "You do not have permission to view this product");
            return "error/403";
        }

        try {
            ProductResponseDto product = productService.getProductById(productId);
            if (product == null) {
                model.addAttribute("errorMessage", "Product not found");
                return "error/404";
            }

            // Get inventory info
            InventoryResponseDto inventory = null;
            try {
                inventory = inventoryService.getAvailableStock(productId);
            } catch (Exception e) {
                log.warn("Could not load inventory for product {}: {}", productId, e.getMessage());
                // Inventory will be null and template will show placeholder
            }

            // indicate pending state so template can disable actions for unapproved sellers
            boolean isPending = !SellerStatus.APPROVED.equals(seller.getSellerStatus());

            model.addAttribute("title", product.getName() + " - Product Details");
            model.addAttribute("product", product);
            model.addAttribute("inventory", inventory);
            model.addAttribute("user", seller);
            model.addAttribute("sellerId", userId);
            model.addAttribute("isPending", isPending);

            log.info("Seller {} viewed product {}", userId, productId);
            return "seller/products/details";
        } catch (Exception e) {
            log.error("Error viewing product {} for seller {}: {}", productId, userId, e.getMessage(), e);
            model.addAttribute("errorMessage", "Unable to load product details. Please try again.");
            model.addAttribute("title", "Product Details");
            model.addAttribute("product", null);
            return "seller/products/details";
        }
    }

    /**
     * Display form to add new product.
     * Only accessible to APPROVED sellers.
     */
    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Only approved sellers can add products
        if (!SellerStatus.APPROVED.equals(seller.getSellerStatus())) {
            model.addAttribute("user", seller);
            model.addAttribute("status", seller.getSellerStatus());
            return "seller/approval-pending";
        }

        try {
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            List<BrandResponseDto> brands = brandService.getAllBrands();
            
            model.addAttribute("title", "Add New Product");
            model.addAttribute("user", seller);
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            
            log.info("Seller {} opened add product form", userId);
            return "seller/products/add";
        } catch (Exception e) {
            log.error("Error loading add product form for seller: {}", userId, e);
            model.addAttribute("user", seller);
            model.addAttribute("error", "Unable to load product form: " + e.getMessage());
            return "seller/products/add";
        }
    }

    /**
     * Display form to edit a product.
     * Only accessible if seller owns the product.
     */
    @GetMapping("/products/{productId}/edit")
    public String showEditProductForm(
            @PathVariable Long productId,
            Model model) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Verify seller owns the product first
        if (!productService.isSellerOwner(productId, userId)) {
            return "error/403";
        }

        // If seller is not approved allow view-only experience instead of edit
        boolean isPending = !SellerStatus.APPROVED.equals(seller.getSellerStatus());
        if (isPending) {
            return "redirect:/seller/products/" + productId + "/view?pending=true";
        }
        
        try {
            ProductResponseDto product = productService.getProductById(productId);
            if (product == null) {
                model.addAttribute("errorMessage", "Product not found");
                return "error/404";
            }
            
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            List<BrandResponseDto> brands = brandService.getAllBrands();
            
            model.addAttribute("title", "Edit Product");
            model.addAttribute("user", seller);
            model.addAttribute("product", product);
            model.addAttribute("productId", productId);
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            
            log.info("Seller {} opened edit product form for product: {}", userId, productId);
            return "seller/products/edit";
        } catch (Exception e) {
            log.error("Error loading edit product form for seller: {}", userId, e);
            model.addAttribute("user", seller);
            model.addAttribute("error", "Unable to load product form: " + e.getMessage());
            return "seller/products/edit";
        }
    }

    /**
     * Delete a product.
     * Only accessible if seller owns the product.
     */
    @PostMapping("/products/{productId}/delete")
    public String deleteProduct(
            @PathVariable Long productId,
            RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Verify seller owns the product
        if (!productService.isSellerOwner(productId, userId)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to delete this product");
            return "redirect:/seller/products";
        }
        
        try {
            productService.deleteProduct(productId);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete product: " + e.getMessage());
        }

        return "redirect:/seller/products";
    }
}
