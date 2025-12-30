package com.abhishek.ecommerce.inventory.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto;
import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Inventory APIs
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ========================= INCREASE STOCK =========================
    @PutMapping("/products/{productId}/stock/increase")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<InventoryResponseDto> increaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequestDto requestDto
    ) {
        InventoryResponseDto response = inventoryService.increaseStock(productId, requestDto);
        return ApiResponseBuilder.success("Stock increased successfully", response);
    }

    // ========================= REDUCE STOCK =========================
    @PutMapping("/products/{productId}/stock/reduce")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<InventoryResponseDto> reduceStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequestDto requestDto
    ) {
        InventoryResponseDto response = inventoryService.reduceStock(productId, requestDto);
        return ApiResponseBuilder.success("Stock reduced successfully", response);
    }

    // ========================= GET STOCK =========================
    @GetMapping("/products/{productId}/stock")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<InventoryResponseDto> getStock(@PathVariable Long productId) {
        InventoryResponseDto response = inventoryService.getAvailableStock(productId);
        return ApiResponseBuilder.success("Stock fetched successfully", response);
    }
}
