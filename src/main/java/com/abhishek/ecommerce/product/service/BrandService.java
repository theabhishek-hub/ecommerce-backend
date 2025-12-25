package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.entity.Brand;

import java.util.List;

public interface BrandService {
    Brand createBrand(Brand brand);
    Brand getBrandById(Long brandId);
    List<Brand> getAllBrands();
    Brand updateBrand(Long brandId, Brand brand);
    void deactivateBrand(Long brandId);
    void activateBrand(Long brandId);
    List<Brand> getAllActiveBrands();
    void deleteBrand(Long brandId);
}

