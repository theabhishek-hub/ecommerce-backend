package com.abhishek.ecommerce.user.controller;
import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for User APIs.
 **/

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ================= CREATE =================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ApiResponseBuilder.created("User created successfully", createdUser);
    }

    // ================= READ =================

    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ApiResponseBuilder.success("User fetched successfully", user);
    }

    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ApiResponseBuilder.success("Users fetched successfully", users);
    }

    @GetMapping("/active")
    public ApiResponse<List<User>> getAllActiveUsers() {
        List<User> users = userService.getAllActiveUsers();
        return ApiResponseBuilder.success("Active users fetched successfully", users);
    }

    // ================= UPDATE =================

    @PutMapping("/{userId}")
    public ApiResponse<User> updateUser(
            @PathVariable Long userId,
            @RequestBody User user) {

        User updatedUser = userService.updateUser(userId, user);
        return ApiResponseBuilder.success("User updated successfully", updatedUser);
    }

    // ================= SOFT DELETE =================

    @PatchMapping("/{userId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ApiResponseBuilder.success("User deactivated successfully");
    }

    @PatchMapping("/{userId}/activate")
    public ApiResponse<Void> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ApiResponseBuilder.success("User activated successfully");
    }

    // ================= HARD DELETE (OPTIONAL) =================

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponseBuilder.success("User deleted successfully");
    }
}
