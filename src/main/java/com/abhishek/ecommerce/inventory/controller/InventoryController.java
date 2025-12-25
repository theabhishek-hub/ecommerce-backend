package com.abhishek.ecommerce.inventory.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
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

    @PatchMapping("/products/{productId}/stock/increase")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> increaseStock(
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        inventoryService.increaseStock(productId, quantity);
        return ApiResponseBuilder.success("Stock increased successfully");
    }

    @PatchMapping("/products/{productId}/stock/reduce")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> reduceStock(@PathVariable Long productId, @RequestParam int quantity) {
        inventoryService.reduceStock(productId, quantity);
        return ApiResponseBuilder.success("Stock reduced successfully");
    }

    @GetMapping("/products/{productId}/stock")
    public ApiResponse<Integer> getStock(@PathVariable Long productId) {
        return ApiResponseBuilder.success("Stock fetched successfully", inventoryService.getAvailableStock(productId));
    }
}
