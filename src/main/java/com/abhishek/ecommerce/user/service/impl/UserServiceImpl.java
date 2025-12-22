package com.abhishek.ecommerce.user.service.impl;

import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Concrete implementation of UserService.
 *
 * Contains:
 * - Business rules
 * - Transaction boundaries (later)
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        // In real systems:
        // - password hashing
        // - duplicate email checks
        // will be added later (security phase)
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + userId));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long userId, User updatedUser) {
        User existingUser = getUserById(userId);

        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setStatus(updatedUser.getStatus());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}

