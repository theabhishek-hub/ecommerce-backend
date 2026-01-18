package com.abhishek.ecommerce.seller.repository;

import com.abhishek.ecommerce.seller.entity.Seller;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    /**
     * Find seller by user ID (One-to-One relationship)
     */
    Optional<Seller> findByUserId(Long userId);

    /**
     * Check if a user already has a seller profile
     */
    boolean existsByUserId(Long userId);

    /**
     * Find all sellers with a specific status
     */
    List<Seller> findByStatus(SellerStatus status);

    /**
     * Count sellers by status
     */
    long countByStatus(SellerStatus status);

    // ========================= PAGINATION & SEARCH =========================
    /**
     * Get all sellers with pagination
     */
    Page<Seller> findAll(Pageable pageable);

    /**
     * Find sellers by status with pagination
     */
    Page<Seller> findByStatus(SellerStatus status, Pageable pageable);
}
