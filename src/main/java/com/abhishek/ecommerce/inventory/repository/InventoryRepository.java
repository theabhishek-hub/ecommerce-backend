package com.abhishek.ecommerce.inventory.repository;

import com.abhishek.ecommerce.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Inventory repository
 */
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i WHERE i.product.seller.id = :sellerId")
    Page<Inventory> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.product.seller.id = :sellerId AND LOWER(i.product.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Inventory> findBySellerIdAndProductNameContaining(@Param("sellerId") Long sellerId, @Param("name") String name, Pageable pageable);
}
