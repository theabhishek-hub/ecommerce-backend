package com.abhishek.ecommerce.product.repository;

import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.shared.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>
{
    @EntityGraph(attributePaths = {"category", "brand"})
    Optional<Product> findById(Long id);

    List<Product> findAll();

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findAll(Pageable pageable);

    List<Product> findAllByStatus(ProductStatus status);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);
    
    boolean existsBySku(String sku);
    
    Optional<Product> findBySku(String sku);

    // Filtering methods
    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByBrandId(Long brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByCategoryIdAndBrandId(Long categoryId, Long brandId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("SELECT p FROM Product p WHERE p.price.amount BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("SELECT p FROM Product p WHERE p.status = :status AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByStatusAndNameContainingIgnoreCase(@Param("status") ProductStatus status, @Param("name") String name, Pageable pageable);

    // Count operations
    long countByStatus(ProductStatus status);

    // Seller operations
    List<Product> findBySellerId(Long sellerId);

    List<Product> findBySellerIdAndStatus(Long sellerId, ProductStatus status);

    boolean existsByIdAndSellerId(Long productId, Long sellerId);
}