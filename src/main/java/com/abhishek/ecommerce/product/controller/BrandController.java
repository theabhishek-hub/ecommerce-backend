package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
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

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    // ========================= CREATE =========================
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

    // ========================= ACTIVATE =========================
    @PutMapping("/{brandId}/activate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activateBrand(@PathVariable Long brandId) {
        brandService.activateBrand(brandId);
        return ApiResponseBuilder.success("Brand activated successfully", null);
    }

    // ========================= DEACTIVATE =========================
    @PutMapping("/{brandId}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivateBrand(@PathVariable Long brandId) {
        brandService.deactivateBrand(brandId);
        return ApiResponseBuilder.success("Brand deactivated successfully", null);
    }

    // ========================= DELETE (SOFT DELETE) =========================
    @DeleteMapping("/{brandId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBrand(@PathVariable Long brandId) {
        brandService.deleteBrand(brandId);
        return ApiResponseBuilder.success("Brand deleted successfully", null);
    }
}
