package com.abhishek.ecommerce.order.repository;

import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.shared.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "items.product"})
    List<Order> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "items.product"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items.product"})
    Optional<Order> findById(Long id);

    long countByStatus(OrderStatus status);

    /**
     * Find all orders that contain items from a specific seller's products
     */
    @EntityGraph(attributePaths = {"user", "items.product.seller"})
    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.items oi " +
            "JOIN oi.product p " +
            "WHERE p.seller.id = :sellerId")
    List<Order> findOrdersContainingSeller(@Param("sellerId") Long sellerId);

    /**
     * Find paginated orders for a seller
     */
    @EntityGraph(attributePaths = {"user", "items.product.seller"})
    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.items oi " +
            "JOIN oi.product p " +
            "WHERE p.seller.id = :sellerId")
    Page<Order> findOrdersContainingSeller(@Param("sellerId") Long sellerId, Pageable pageable);
}