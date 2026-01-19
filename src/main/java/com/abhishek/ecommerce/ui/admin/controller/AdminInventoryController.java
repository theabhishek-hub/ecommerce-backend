package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Admin Inventory Controller
 * Access: ROLE_ADMIN only
 */
@Slf4j
@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    /**
     * GET /admin/inventory
     * List all inventory (read-only overview)
     */
    @GetMapping
    public String listInventory(
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, 10, Sort.by("product.name").ascending());
            var inventoryPage = inventoryService.getAllInventory(pageable);
            
            model.addAttribute("title", "Inventory Overview");
            model.addAttribute("inventoryList", inventoryPage.getContent());
            model.addAttribute("page", inventoryPage);
            model.addAttribute("hasInventory", !inventoryPage.isEmpty());
            
            log.info("Admin inventory overview loaded. Total: {}", inventoryPage.getTotalElements());
            return "admin/inventory/list";
        } catch (Exception e) {
            log.error("Error loading admin inventory overview", e);
            model.addAttribute("errorMessage", "Unable to load inventory overview. Please try again.");
            return "admin/inventory/list";
        }
    }
}
