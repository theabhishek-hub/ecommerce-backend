package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.entity.Brand;

import java.util.List;

public interface BrandService {
    Brand create(Brand brand);
    Brand getById(Long id);
    List<Brand> getAll();
    Brand update(Long id, Brand brand);
    void deactivate(Long id);
}

