package com.abhishek.ecommerce.inventory.controller;

import com.abhishek.ecommerce.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.*;

/**
 * Inventory APIs
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Add stock for a product (Admin use)
     */
    @PostMapping("/add/{productId}")
    public void addStock(
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        inventoryService.addStock(productId, quantity);
    }

    /**
     * Get available stock
     */
    @GetMapping("/{productId}")
    public int getStock(@PathVariable Long productId) {
        return inventoryService.getAvailableStock(productId);
    }
}

