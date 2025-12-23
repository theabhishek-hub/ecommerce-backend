package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
