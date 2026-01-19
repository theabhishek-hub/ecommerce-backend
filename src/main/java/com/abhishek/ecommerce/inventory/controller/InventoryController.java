package com.abhishek.ecommerce.inventory.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
import com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto;
import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.seller.service.SellerService;
import com.abhishek.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserService userService;
    private final SellerService sellerService;
    private final ProductService productService;

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

    // ========================= GET STOCK (PUBLIC) =========================
    @Operation(
        summary = "Get product stock",
        description = "Public endpoint - accessible to all users"
    )
    @GetMapping("/products/{productId}/stock")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<InventoryResponseDto> getStock(@PathVariable Long productId) {
        InventoryResponseDto response = inventoryService.getAvailableStock(productId);
        return ApiResponseBuilder.success("Stock fetched successfully", response);
    }

    // ========================= SELLER ENDPOINTS =========================
    
    // ========================= SELLER: GET INVENTORY LIST =========================
    @Operation(
        summary = "Get seller inventory list",
        description = "Requires SELLER role"
    )
    @GetMapping("/seller/inventory")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<PageResponseDto<InventoryResponseDto>> getSellerInventory(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "product.name") Pageable pageable) {
        var currentUser = userService.getCurrentUserProfile();
        var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
        
        if (sellerProfile == null) {
            throw new IllegalStateException("Seller profile not found");
        }
        
        PageResponseDto<InventoryResponseDto> response;
        if (q != null && !q.trim().isEmpty()) {
            response = inventoryService.getInventoryBySellerAndSearch(sellerProfile.getId(), q.trim(), pageable);
        } else {
            response = inventoryService.getInventoryBySeller(sellerProfile.getId(), pageable);
        }
        
        return ApiResponseBuilder.success("Inventory fetched successfully", response);
    }

    // ========================= SELLER: UPDATE STOCK (INCREASE) =========================
    @Operation(
        summary = "Seller increase product stock",
        description = "Requires SELLER role and product ownership"
    )
    @PutMapping("/seller/products/{productId}/stock/increase")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<InventoryResponseDto> sellerIncreaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequestDto requestDto) {
        var currentUser = userService.getCurrentUserProfile();
        var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
        
        if (sellerProfile == null) {
            throw new IllegalStateException("Seller profile not found");
        }
        
        // Validate ownership
        if (!productService.isSellerOwner(productId, sellerProfile.getId())) {
            throw new ProductNotFoundException(productId);
        }
        
        InventoryResponseDto response = inventoryService.increaseStock(productId, requestDto);
        return ApiResponseBuilder.success("Stock increased successfully", response);
    }

    // ========================= SELLER: UPDATE STOCK (REDUCE) =========================
    @Operation(
        summary = "Seller reduce product stock",
        description = "Requires SELLER role and product ownership"
    )
    @PutMapping("/seller/products/{productId}/stock/reduce")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<InventoryResponseDto> sellerReduceStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequestDto requestDto) {
        var currentUser = userService.getCurrentUserProfile();
        var sellerProfile = sellerService.getSellerByUserId(currentUser.getId());
        
        if (sellerProfile == null) {
            throw new IllegalStateException("Seller profile not found");
        }
        
        // Validate ownership
        if (!productService.isSellerOwner(productId, sellerProfile.getId())) {
            throw new ProductNotFoundException(productId);
        }
        
        InventoryResponseDto response = inventoryService.reduceStock(productId, requestDto);
        return ApiResponseBuilder.success("Stock reduced successfully", response);
    }

    // ========================= ADMIN: GET ALL INVENTORY =========================
    @Operation(
        summary = "Get all inventory (admin)",
        description = "Requires ADMIN role"
    )
    @GetMapping("/admin/inventory")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponseDto<InventoryResponseDto>> getAllInventory(
            @PageableDefault(size = 10, sort = "product.name") Pageable pageable) {
        PageResponseDto<InventoryResponseDto> response = inventoryService.getAllInventory(pageable);
        return ApiResponseBuilder.success("Inventory fetched successfully", response);
    }
}
