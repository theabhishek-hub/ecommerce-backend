package com.abhishek.ecommerce.user.repository;

import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


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
}