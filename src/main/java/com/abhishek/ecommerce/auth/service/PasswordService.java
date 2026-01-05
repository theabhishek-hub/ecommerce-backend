package com.abhishek.ecommerce.auth.service;

public interface PasswordService {
    void changePassword(String currentPassword, String newPassword);
    void requestPasswordReset(String email);
    void resetPassword(String resetToken, String newPassword);
}










