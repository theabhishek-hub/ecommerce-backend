package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.service.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ApiResponse<Brand> createBrand(@RequestBody Brand brand) {
        return ApiResponseBuilder.success("Brand created successfully", brandService.createBrand(brand));
    }

    @GetMapping
    public ApiResponse<List<Brand>> getAllBrands() {
        return ApiResponseBuilder.success("Brands fetched successfully", brandService.getAllBrands());
    }

    @GetMapping("/{brandId}")
    public ApiResponse<Brand> getBrandById(@PathVariable Long brandId) {
        return ApiResponseBuilder.success("Brand fetched successfully", brandService.getBrandById(brandId));
    }

    @PutMapping("/{brandId}")
    public ApiResponse<Brand> updateBrand(@PathVariable Long brandId, @RequestBody Brand brand) {
        return ApiResponseBuilder.success("Brand updated successfully", brandService.updateBrand(brandId, brand));
    }

    @PatchMapping("/{brandId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivate(@PathVariable Long brandId) {
        brandService.deactivateBrand(brandId);
        return ApiResponseBuilder.success("Brand deactivated successfully");
    }

    @PatchMapping("/{brandId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activate(@PathVariable Long brandId) {
        brandService.activateBrand(brandId);
        return ApiResponseBuilder.success("Brand activated successfully");
    }

    @GetMapping("/active")
    public ApiResponse<List<Brand>> getAllActiveBrands() {
        return ApiResponseBuilder.success("Active brands fetched successfully", brandService.getAllActiveBrands());
    }

    @DeleteMapping("/{brandId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long brandId) {
        brandService.deleteBrand(brandId);
        return ApiResponseBuilder.success("Brand deleted successfully");
    }

}
