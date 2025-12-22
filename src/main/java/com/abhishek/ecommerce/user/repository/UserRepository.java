package com.abhishek.ecommerce.user.repository;

import com.abhishek.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository responsible for User persistence.
 *
 * - No business logic
 * - No validations
 * - Only database access
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Used during login / registration checks.
     */
    Optional<User> findByEmail(String email);
}
