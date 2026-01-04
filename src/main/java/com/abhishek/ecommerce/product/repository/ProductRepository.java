package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>
{
    List<Product> findAll();

    List<Product> findAllByStatus(ProductStatus status);
    
    boolean existsBySku(String sku);
    
    Optional<Product> findBySku(String sku);
}
