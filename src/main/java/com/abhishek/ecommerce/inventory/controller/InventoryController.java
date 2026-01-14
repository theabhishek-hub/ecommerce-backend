package com.abhishek.ecommerce.inventory.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto;
import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Inventory APIs
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@Tag(name = "Inventory", description = "Inventory management APIs")
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ========================= INCREASE STOCK =========================
    @Operation(
        summary = "Increase product stock",
        description = "Requires ADMIN role"
    )
    @PutMapping("/products/{productId}/stock/increase")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InventoryResponseDto> increaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequestDto requestDto
    ) {
        InventoryResponseDto response = inventoryService.increaseStock(productId, requestDto);
        return ApiResponseBuilder.success("Stock increased successfully", response);
    }

    // ========================= REDUCE STOCK =========================
    @Operation(
        summary = "Reduce product stock",
        description = "Requires ADMIN role"
    )
    @PutMapping("/products/{productId}/stock/reduce")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InventoryResponseDto> reduceStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequestDto requestDto
    ) {
        InventoryResponseDto response = inventoryService.reduceStock(productId, requestDto);
        return ApiResponseBuilder.success("Stock reduced successfully", response);
    }

    // ========================= GET STOCK =========================
    @Operation(
        summary = "Get product stock",
        description = "Requires ADMIN role"
    )
    @GetMapping("/products/{productId}/stock")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InventoryResponseDto> getStock(@PathVariable Long productId) {
        InventoryResponseDto response = inventoryService.getAvailableStock(productId);
        return ApiResponseBuilder.success("Stock fetched successfully", response);
    }
}
