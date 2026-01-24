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
            // Use simple sort without nested properties to avoid issues
            Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
            var inventoryPageResponse = inventoryService.getAllInventory(pageable);
            
            // Extract the Page object for template pagination
            // The template needs Spring Data Page for pagination methods
            var inventoryList = inventoryPageResponse.getContent();
            
            model.addAttribute("title", "Inventory Overview");
            model.addAttribute("inventoryList", inventoryList);
            model.addAttribute("pageNumber", page);
            model.addAttribute("totalPages", inventoryPageResponse.getTotalPages());
            model.addAttribute("totalElements", inventoryPageResponse.getTotalElements());
            model.addAttribute("hasInventory", !inventoryList.isEmpty());
            
            log.info("Admin inventory overview loaded. Total: {}", inventoryPageResponse.getTotalElements());
            return "admin/inventory/list";
        } catch (Exception e) {
            log.error("Error loading admin inventory overview", e);
            model.addAttribute("errorMessage", "Unable to load inventory overview. Please try again.");
            model.addAttribute("inventoryList", java.util.Collections.emptyList());
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalElements", 0);
            model.addAttribute("pageNumber", 0);
            model.addAttribute("hasInventory", false);
            return "admin/inventory/list";
        }
    }
}
