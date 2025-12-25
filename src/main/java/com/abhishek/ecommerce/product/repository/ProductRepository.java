package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>
{
    List<Product> findAllByStatus(ProductStatus status);
}
