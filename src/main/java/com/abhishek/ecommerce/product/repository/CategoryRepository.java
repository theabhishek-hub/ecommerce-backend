package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

