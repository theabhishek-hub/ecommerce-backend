package com.abhishek.ecommerce.user.service;

import com.abhishek.ecommerce.user.entity.User;

import java.util.List;

/**
 * Defines business operations related to users.
 *
 * This interface hides persistence details from controllers.
 */
public interface UserService {

    User createUser(User user);

    User getUserById(Long userId);

    List<User> getAllUsers();

    User updateUser(Long userId, User updatedUser);

    void deleteUser(Long userId);
}

