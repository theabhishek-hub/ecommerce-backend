package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.entity.BrandStatus;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    public Brand createBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    public Brand getBrandById(Long brandId) {
        return brandRepository.findById(brandId).orElseThrow();
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand updateBrand(Long brandId, Brand brand) {
        Brand existing = getBrandById(brandId);
        existing.setName(brand.getName());
        existing.setDescription(brand.getDescription());
        existing.setCountry(brand.getCountry());
        return brandRepository.save(existing);
    }

    @Override
    @Transactional
    public void deactivateBrand(Long brandId) {
        Brand brand = getBrand(brandId);
        brand.setStatus(BrandStatus.INACTIVE);
        brandRepository.save(brand);
    }

    @Override
    @Transactional
    public void activateBrand(Long brandId) {
        Brand brand = getBrand(brandId);
        brand.setStatus(BrandStatus.ACTIVE);
        brandRepository.save(brand);
    }

    private Brand getBrand(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
    }
    public List<Brand> getAllActiveBrands() {
        return brandRepository.findAllByStatus(BrandStatus.ACTIVE);
    }

    @Override
    public void deleteBrand(Long brandId) {
        Brand brand = getBrandById(brandId);
        brand.setStatus(BrandStatus.INACTIVE);
        brandRepository.save(brand);
    }

}

