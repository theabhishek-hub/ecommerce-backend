package com.abhishek.ecommerce.user.repository;

import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.QueryHint;


public interface UserRepository extends JpaRepository<User, Long> {

    // used for create user duplicate check
    boolean existsByEmail(String email);

    /**
     * Used during login / registration checks.
     */
    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    List<User> findAllByStatus(UserStatus status);

    // Seller-related queries
    List<User> findBySellerStatus(SellerStatus status);
    long countByStatus(UserStatus status);

    long countBySellerStatus(SellerStatus status);

    /**
     * CRITICAL: Fetch fresh user from database, bypassing Hibernate cache
     * Used to get latest SellerStatus after admin approval
     * QueryHint(name = "org.hibernate.cacheable", value = "false") ensures fresh read
     */
    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.status = :status")
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "false")})
    Optional<User> findFreshByIdAndStatus(@Param("userId") Long userId, @Param("status") UserStatus status);
}