package com.abhishek.ecommerce.user.repository;

import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Used during login / registration checks.
     */
    Optional<User> findByEmail(String email);

    List<User> findAllByStatus(UserStatus status);
}
