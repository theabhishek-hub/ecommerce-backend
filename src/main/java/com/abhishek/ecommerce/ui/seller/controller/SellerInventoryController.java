package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.product.service.ProductService;
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

/**
 * UI Controller for seller inventory management pages.
 * Handles seller's stock management for own products.
 */
@Slf4j
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerInventoryController {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final ProductService productService;

    /**
     * Display seller's inventory listing page.
     * Shows all products and their stock levels.
     * Only accessible to APPROVED sellers.
     */
    @GetMapping("/inventory")
    public String listInventory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "q", required = false) String search,
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
            // Load inventory data for this seller
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
            var inventoryPage = search != null && !search.trim().isEmpty() 
                ? inventoryService.getInventoryBySellerAndSearch(userId, search.trim(), pageable)
                : inventoryService.getInventoryBySeller(userId, pageable);
            
            model.addAttribute("inventoryList", inventoryPage.getContent());
            model.addAttribute("page", inventoryPage);
            model.addAttribute("hasInventory", inventoryPage.getContent() != null && !inventoryPage.getContent().isEmpty());
            model.addAttribute("searchQuery", search != null ? search : "");
        } catch (Exception e) {
            log.error("Error loading inventory for seller {}: {}", userId, e.getMessage(), e);
            model.addAttribute("inventoryList", java.util.Collections.emptyList());
            model.addAttribute("hasInventory", false);
            model.addAttribute("error", "Failed to load inventory: " + e.getMessage());
        }

        model.addAttribute("title", "Inventory Management");
        model.addAttribute("user", seller);
        model.addAttribute("sellerId", userId);
        model.addAttribute("isPending", isPending);
        model.addAttribute("status", seller.getSellerStatus());
        return "seller/inventory/list";
    }

    /**
     * Display inventory detail page for a specific product.
     * Shows detailed stock information and history.
     */
    @GetMapping("/inventory/{productId}")
    public String viewInventoryDetail(
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
            return "error/403";
        }

        // Allow pending sellers to view inventory in read-only mode. Templates will check isPending.
        boolean isPending = !SellerStatus.APPROVED.equals(seller.getSellerStatus());
        
        try {
            var inventory = inventoryService.getAvailableStock(productId);
            model.addAttribute("title", "Inventory Details");
            model.addAttribute("user", seller);
            model.addAttribute("productId", productId);
            model.addAttribute("inventory", inventory);
            model.addAttribute("isPending", isPending);
        } catch (Exception e) {
            log.error("Error loading inventory for product {}: {}", productId, e.getMessage());
            model.addAttribute("error", "Failed to load inventory details");
        }
        
        return "seller/inventory/detail";
    }

    /**
     * Increase stock for a product.
     * Only accessible if seller owns the product.
     */
    @PostMapping("/inventory/{productId}/increase")
    public String increaseStock(
            @PathVariable Long productId,
            @RequestParam(value = "quantity") Integer quantity,
            RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId).orElse(null);
        // Verify seller owns the product
        if (!productService.isSellerOwner(productId, userId)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to modify this inventory");
            return "redirect:/seller/inventory";
        }

        // Only approved sellers may modify stock
        if (seller == null || !SellerStatus.APPROVED.equals(seller.getSellerStatus())) {
            redirectAttributes.addFlashAttribute("error", "Your seller account is pending approval — stock modifications are disabled.");
            return "redirect:/seller/inventory/" + productId;
        }

        try {
            UpdateStockRequestDto requestDto = new UpdateStockRequestDto();
            requestDto.setQuantity(quantity);
            inventoryService.increaseStock(productId, requestDto);
            redirectAttributes.addFlashAttribute("success", "Stock increased successfully");
        } catch (Exception e) {
            log.error("Error increasing stock for product {}: {}", productId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to increase stock: " + e.getMessage());
        }

        return "redirect:/seller/inventory";
    }

    /**
     * Decrease stock for a product.
     * Only accessible if seller owns the product.
     */
    @PostMapping("/inventory/{productId}/decrease")
    public String decreaseStock(
            @PathVariable Long productId,
            @RequestParam(value = "quantity") Integer quantity,
            RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User seller = userRepository.findById(userId).orElse(null);
        // Verify seller owns the product
        if (!productService.isSellerOwner(productId, userId)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to modify this inventory");
            return "redirect:/seller/inventory";
        }

        // Only approved sellers may modify stock
        if (seller == null || !SellerStatus.APPROVED.equals(seller.getSellerStatus())) {
            redirectAttributes.addFlashAttribute("error", "Your seller account is pending approval — stock modifications are disabled.");
            return "redirect:/seller/inventory/" + productId;
        }

        try {
            UpdateStockRequestDto requestDto = new UpdateStockRequestDto();
            requestDto.setQuantity(quantity);
            inventoryService.reduceStock(productId, requestDto);
            redirectAttributes.addFlashAttribute("success", "Stock decreased successfully");
        } catch (Exception e) {
            log.error("Error decreasing stock for product {}: {}", productId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to decrease stock: " + e.getMessage());
        }

        return "redirect:/seller/inventory";
    }
}
