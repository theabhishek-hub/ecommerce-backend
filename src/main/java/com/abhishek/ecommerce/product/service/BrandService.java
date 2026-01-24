package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.dto.request.BrandCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.BrandUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;

import java.util.List;

public interface BrandService {
    
    // CREATE
    BrandResponseDto createBrand(BrandCreateRequestDto requestDto);
    
    // READ
    BrandResponseDto getBrandById(Long brandId);
    List<BrandResponseDto> getAllBrands();
    List<BrandResponseDto> getAllActiveBrands();
    
    // SEARCH
    List<BrandResponseDto> searchBrandsByName(String name);
    
    // FILTER
    List<BrandResponseDto> filterByStatus(String status);
    
    // SORT
    List<BrandResponseDto> getAllBrandsSorted(String sortBy, String order);
    
    // COMBINED SEARCH & FILTER & SORT
    List<BrandResponseDto> searchFilterSort(String name, String status, String sortBy, String order);
    
    // UPDATE
    BrandResponseDto updateBrand(Long brandId, BrandUpdateRequestDto requestDto);
    
    // STATUS OPERATIONS
    void activateBrand(Long brandId);
    void deactivateBrand(Long brandId);
    
    // DELETE (soft delete)
    void deleteBrand(Long brandId);
}

