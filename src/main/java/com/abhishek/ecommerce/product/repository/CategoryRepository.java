package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Category;
import com.abhishek.ecommerce.product.entity.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>
{
    List<Category> findAllByStatus(CategoryStatus status);
}

