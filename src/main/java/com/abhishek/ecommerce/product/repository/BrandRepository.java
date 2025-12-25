package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.entity.BrandStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long>
{
    List<Brand> findAllByStatus(BrandStatus status);
}

