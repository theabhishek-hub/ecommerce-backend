package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.service.UserService;
import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.product.service.CategoryService;
import com.abhishek.ecommerce.product.service.BrandService;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Seller Products Controller
 * Access: ROLE_SELLER with SellerStatus = APPROVED ONLY
 * Unapproved sellers are redirected to approval pending page
 * Sellers can view/manage only their own products
 */
@Slf4j
@Controller
@RequestMapping("/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {

    private final UserService userService;
    private final SellerService sellerService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    /**
     * GET /seller/products
     * List seller's products
     * BLOCKS access if seller is NOT APPROVED
     */
    @GetMapping
    public String listProducts(Model model, RedirectAttributes redirectAttributes) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            
            // CRITICAL FIX: Force refresh user data to get latest SellerStatus from database
            var refreshedUser = userService.getUserById(currentUser.getId());
            
            // CRITICAL: Check seller profile status FIRST (source of truth)
            var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
            String status = null;
            
            if (sellerProfile != null && sellerProfile.getStatus() != null) {
                // Use seller profile status as source of truth
                status = sellerProfile.getStatus();
                refreshedUser.setSellerStatus(status);
            } else {
                // Fallback to user's sellerStatus if no seller profile exists
                status = refreshedUser.getSellerStatus();
            }
            
            // Check seller approval status
            if (status == null || !SellerStatus.APPROVED.name().equals(status)) {
                log.warn("Unapproved seller attempted product access. UserId={}, Status={}", 
                    currentUser.getId(), status);
                model.addAttribute("error", "Your seller account must be approved before accessing this page.");
                model.addAttribute("user", refreshedUser);
                model.addAttribute("title", "Access Denied");
                return "seller/approval-pending";
            }
            
            // Fetch seller's products
            List<ProductResponseDto> products = productService.getProductsBySeller(currentUser.getId());
            
            model.addAttribute("user", refreshedUser);
            model.addAttribute("title", "My Products");
            model.addAttribute("products", products);
            model.addAttribute("hasProducts", !products.isEmpty());
            
            log.info("Seller products page loaded for userId={}, Products count={}", 
                currentUser.getId(), products.size());
            return "seller/products/list";
        } catch (Exception e) {
            log.error("Error loading seller products", e);
            model.addAttribute("error", "Unable to load products");
            return "seller/products/list";
        }
    }

    /**
     * GET /seller/products/add
     * Show add product form
     * BLOCKS access if seller is NOT APPROVED
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            var refreshedUser = userService.getUserById(currentUser.getId());
            
            // Check seller approval status
            var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
            String status = null;
            
            if (sellerProfile != null && sellerProfile.getStatus() != null) {
                status = sellerProfile.getStatus();
                refreshedUser.setSellerStatus(status);
            } else {
                status = refreshedUser.getSellerStatus();
            }
            
            if (status == null || !SellerStatus.APPROVED.name().equals(status)) {
                log.warn("Unapproved seller attempted to access add product form. UserId={}", currentUser.getId());
                model.addAttribute("error", "Your seller account must be approved before adding products.");
                model.addAttribute("user", refreshedUser);
                model.addAttribute("title", "Access Denied");
                return "seller/approval-pending";
            }
            
            // Fetch categories and brands for dropdown
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            List<BrandResponseDto> brands = brandService.getAllBrands();
            
            model.addAttribute("user", refreshedUser);
            model.addAttribute("title", "Add New Product");
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("product", new ProductCreateRequestDto());
            
            log.info("Add product form displayed for seller userId={}", currentUser.getId());
            return "seller/products/add";
        } catch (Exception e) {
            log.error("Error loading add product form", e);
            model.addAttribute("error", "Unable to load product form");
            return "seller/products/add";
        }
    }

    /**
     * POST /seller/products
     * Create a new product
     * BLOCKS access if seller is NOT APPROVED
     */
    @PostMapping
    public String createProduct(@Valid @ModelAttribute ProductCreateRequestDto productDto, 
                               Model model, RedirectAttributes redirectAttributes) {
        var currentUser = userService.getCurrentUserProfile();
        try {
            var refreshedUser = userService.getCurrentUserProfile();
            
            // Check seller approval status
            var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
            String status = null;
            
            if (sellerProfile != null && sellerProfile.getStatus() != null) {
                status = sellerProfile.getStatus();
            } else {
                status = refreshedUser.getSellerStatus();
            }
            
            if (status == null || !SellerStatus.APPROVED.name().equals(status)) {
                log.warn("Unapproved seller attempted to create product. UserId={}", currentUser.getId());
                redirectAttributes.addFlashAttribute("error", "Your seller account must be approved to add products.");
                return "redirect:/seller/products";
            }
            
            // Create product
            ProductResponseDto createdProduct = productService.createProduct(productDto);
            
            log.info("Product created successfully by seller userId={}. ProductId={}", 
                currentUser.getId(), createdProduct.getId());
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
            return "redirect:/seller/products";
        } catch (Exception e) {
            log.error("Error creating product for seller userId={}", currentUser.getId(), e);
            model.addAttribute("error", "Unable to add product: " + e.getMessage());
            model.addAttribute("title", "Add New Product");
            model.addAttribute("product", productDto);
            return "seller/products/add";
        }
    }
}