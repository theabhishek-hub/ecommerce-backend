package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    public Brand create(Brand brand) {
        return brandRepository.save(brand);
    }

    public Brand getById(Long id) {
        return brandRepository.findById(id).orElseThrow();
    }

    public List<Brand> getAll() {
        return brandRepository.findAll();
    }

    public Brand update(Long id, Brand brand) {
        Brand existing = getById(id);
        existing.setName(brand.getName());
        existing.setDescription(brand.getDescription());
        existing.setCountry(brand.getCountry());
        existing.setActive(brand.getActive());
        return brandRepository.save(existing);
    }

    public void deactivate(Long id) {
        Brand brand = getById(id);
        brand.setActive(false);
        brandRepository.save(brand);
    }
}

