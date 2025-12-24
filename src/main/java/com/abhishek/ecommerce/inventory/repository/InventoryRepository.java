package com.abhishek.ecommerce.inventory.repository;

import com.abhishek.ecommerce.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Inventory repository
 */
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);
}
