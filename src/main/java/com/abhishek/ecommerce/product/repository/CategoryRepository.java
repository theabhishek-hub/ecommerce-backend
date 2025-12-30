package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.product.entity.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>
{
    List<Category> findAllByStatus(CategoryStatus status);
    
    boolean existsByName(String name);
    
    Optional<Category> findByName(String name);
}

