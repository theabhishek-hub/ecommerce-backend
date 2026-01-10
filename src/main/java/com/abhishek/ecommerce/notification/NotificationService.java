package com.abhishek.ecommerce.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Notification service for non-business side effects
 * Handles order confirmations, emails, and other notifications asynchronously
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    /**
     * Send order confirmation notification asynchronously
     * This is a side effect and should not affect business logic
     */
    @Async
    public void sendOrderConfirmation(Long orderId, String customerEmail, String customerName) {
        try {
            log.info("Sending order confirmation notification for orderId={} to email={}", orderId, customerEmail);

            // Send actual email
            emailService.sendOrderConfirmationEmail(customerEmail, customerName, orderId);

        } catch (Exception e) {
            log.error("Failed to send order confirmation for orderId={}", orderId, e);
            // Don't throw exception - this is a side effect, shouldn't affect business logic
        }
    }

    /**
     * Send order shipped notification asynchronously
     */
    @Async
    public void sendOrderShippedNotification(Long orderId, String customerEmail, String customerName, String trackingNumber) {
        try {
            log.info("Sending order shipped notification for orderId={} to email={}", orderId, customerEmail);

            // Send actual email
            emailService.sendOrderShippedEmail(customerEmail, customerName, orderId, trackingNumber);

        } catch (Exception e) {
            log.error("Failed to send order shipped notification for orderId={}", orderId, e);
        }
    }

    /**
     * Send order delivered notification asynchronously
     */
    @Async
    public void sendOrderDeliveredNotification(Long orderId, String customerEmail, String customerName) {
        try {
            log.info("Sending order delivered notification for orderId={} to email={}", orderId, customerEmail);

            // Send actual email
            emailService.sendOrderDeliveredEmail(customerEmail, customerName, orderId);

        } catch (Exception e) {
            log.error("Failed to send order delivered notification for orderId={}", orderId, e);
        }
    }

    /**
     * Send welcome email to new user asynchronously
     */
    @Async
    public void sendWelcomeEmail(String email, String fullName) {
        try {
            log.info("Sending welcome email to new user: {}", email);

            // Send actual email
            emailService.sendWelcomeEmail(email, fullName);

        } catch (Exception e) {
            log.error("Failed to send welcome email to user: {}", email, e);
        }
    }

    /**
     * Send password reset email asynchronously
     */
    @Async
    public void sendPasswordResetEmail(String email, String resetToken) {
        try {
            log.info("Sending password reset email to: {}", email);

            // Send actual email
            emailService.sendPasswordResetEmail(email, resetToken);

        } catch (Exception e) {
            log.error("Failed to send password reset email to user: {}", email, e);
        }
    }
}