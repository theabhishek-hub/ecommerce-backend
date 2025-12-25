package com.abhishek.ecommerce.user.service;

import com.abhishek.ecommerce.user.entity.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    User getUserById(Long userId);

    List<User> getAllUsers();

    User updateUser(Long userId, User updatedUser);

    void deactivateUser(Long userId);

    void activateUser(Long userId);

    List<User> getAllActiveUsers();

    void deleteUser(Long userId);        // soft delete

}

