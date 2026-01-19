package com.abhishek.ecommerce.ui.seller.controller;

import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Seller Inventory Controller
 * Access: ROLE_SELLER with SellerStatus = APPROVED ONLY
 */
@Slf4j
@Controller
@RequestMapping("/seller/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerInventoryController {

    private final UserService userService;
    private final SellerService sellerService;
    private final InventoryService inventoryService;

    /**
     * GET /seller/inventory
     * List seller's inventory with pagination and search
     */
    @GetMapping
    public String listInventory(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            var refreshedUser = userService.getUserById(currentUser.getId());
            
            var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
            String status = null;
            
            if (sellerProfile != null && sellerProfile.getStatus() != null) {
                status = sellerProfile.getStatus();
                refreshedUser.setSellerStatus(status);
            } else {
                status = refreshedUser.getSellerStatus();
            }
            
            if (status == null || !SellerStatus.APPROVED.name().equals(status)) {
                log.warn("Unapproved seller attempted inventory access. UserId={}, Status={}", 
                    currentUser.getId(), status);
                redirectAttributes.addFlashAttribute("error", "You must be an approved seller to access inventory.");
                return "redirect:/seller/dashboard";
            }
            
            // At this point, sellerProfile is guaranteed to be non-null (checked above)
            Long sellerId = sellerProfile.getId();
            
            // Create pageable
            Pageable pageable = PageRequest.of(page, 10, Sort.by("product.name").ascending());
            
            // Fetch inventory
            var inventoryPage = (q != null && !q.trim().isEmpty()) 
                ? inventoryService.getInventoryBySellerAndSearch(sellerId, q.trim(), pageable)
                : inventoryService.getInventoryBySeller(sellerId, pageable);
            
            model.addAttribute("title", "Inventory Management");
            model.addAttribute("inventoryList", inventoryPage.getContent());
            model.addAttribute("page", inventoryPage);
            model.addAttribute("searchQuery", q != null ? q : "");
            model.addAttribute("hasInventory", !inventoryPage.isEmpty());
            
            log.info("Seller inventory loaded for userId={}. Total: {}", 
                currentUser.getId(), inventoryPage.getTotalElements());
            return "seller/inventory/list";
        } catch (Exception e) {
            log.error("Error loading seller inventory", e);
            model.addAttribute("error", "Unable to load inventory. Please try again.");
            return "seller/inventory/list";
        }
    }
}
