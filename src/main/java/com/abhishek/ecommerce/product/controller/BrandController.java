package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.product.dto.request.BrandCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.BrandUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.product.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "Products", description = "Product APIs (ADMIN manage, USER read)")
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    // ========================= CREATE =========================
    @Operation(
        summary = "Create brand",
        description = "Requires ADMIN role"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponseDto> createBrand(
            @Valid @RequestBody BrandCreateRequestDto requestDto
    ) {
        BrandResponseDto response = brandService.createBrand(requestDto);
        return ApiResponseBuilder.created("Brand created successfully", response);
    }

    // ========================= UPDATE =========================
    @Operation(
        summary = "Update brand",
        description = "Requires ADMIN role"
    )
    @PutMapping("/{brandId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponseDto> updateBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody BrandUpdateRequestDto requestDto
    ) {
        BrandResponseDto response = brandService.updateBrand(brandId, requestDto);
        return ApiResponseBuilder.success("Brand updated successfully", response);
    }

    // ========================= GET BY ID =========================
    @GetMapping("/{brandId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BrandResponseDto> getBrandById(@PathVariable Long brandId) {
        BrandResponseDto response = brandService.getBrandById(brandId);
        return ApiResponseBuilder.success("Brand fetched successfully", response);
    }

    // ========================= GET ALL =========================
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BrandResponseDto>> getAllBrands() {
        List<BrandResponseDto> brands = brandService.getAllBrands();
        return ApiResponseBuilder.success("Brands fetched successfully", brands);
    }

    // ========================= GET ALL ACTIVE =========================
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BrandResponseDto>> getAllActiveBrands() {
        List<BrandResponseDto> brands = brandService.getAllActiveBrands();
        return ApiResponseBuilder.success("Active brands fetched successfully", brands);
    }

    // ========================= SEARCH =========================
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BrandResponseDto>> searchBrands(@RequestParam String name) {
        List<BrandResponseDto> brands = brandService.searchBrandsByName(name);
        return ApiResponseBuilder.success("Brands searched successfully", brands);
    }

    // ========================= FILTER =========================
    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BrandResponseDto>> filterBrands(@RequestParam String status) {
        List<BrandResponseDto> brands = brandService.filterByStatus(status);
        return ApiResponseBuilder.success("Brands filtered successfully", brands);
    }

    // ========================= SORT =========================
    @GetMapping("/sort")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BrandResponseDto>> sortBrands(
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        List<BrandResponseDto> brands = brandService.getAllBrandsSorted(sortBy, order);
        return ApiResponseBuilder.success("Brands sorted successfully", brands);
    }

    // ========================= SEARCH + FILTER + SORT =========================
    @GetMapping("/search-filter-sort")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<BrandResponseDto>> searchFilterSort(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        List<BrandResponseDto> brands = brandService.searchFilterSort(name, status, sortBy, order);
        return ApiResponseBuilder.success("Brands filtered and sorted successfully", brands);
    }

    // ========================= ACTIVATE =========================
    @Operation(
        summary = "Activate brand",
        description = "Requires ADMIN role"
    )
    @PutMapping("/{brandId}/activate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activateBrand(@PathVariable Long brandId) {
        brandService.activateBrand(brandId);
        return ApiResponseBuilder.success("Brand activated successfully", null);
    }

    // ========================= DEACTIVATE =========================
    @Operation(
        summary = "Deactivate brand",
        description = "Requires ADMIN role"
    )
    @PutMapping("/{brandId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivateBrand(@PathVariable Long brandId) {
        brandService.deactivateBrand(brandId);
        return ApiResponseBuilder.success("Brand deactivated successfully", null);
    }

    // ========================= DELETE (SOFT DELETE) =========================
    @Operation(
        summary = "Delete brand",
        description = "Requires ADMIN role"
    )
    @DeleteMapping("/{brandId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBrand(@PathVariable Long brandId) {
        brandService.deleteBrand(brandId);
        return ApiResponseBuilder.success("Brand deleted successfully", null);
    }
}
