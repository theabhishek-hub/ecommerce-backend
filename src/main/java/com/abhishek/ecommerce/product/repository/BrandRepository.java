package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}

