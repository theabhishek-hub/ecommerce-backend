package com.abhishek.ecommerce.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Notification service for non-business side effects
 * Handles order confirmations, emails, and other notifications asynchronously
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * Simulate email sending delay - replace with actual email service in production
     */
    private void simulateEmailDelay() throws InterruptedException {
        Thread.sleep(100); // Simulate network delay
    }

    /**
     * Send order confirmation notification asynchronously
     * This is a side effect and should not affect business logic
     */
    @Async
    public void sendOrderConfirmation(Long orderId, String customerEmail, String customerName) {
        try {
            log.info("Sending order confirmation notification for orderId={} to email={}", orderId, customerEmail);

            // Simulate email sending delay - in production, replace with actual email service call
            simulateEmailDelay();

            // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
            // For now, just log the notification
            log.info("Order confirmation sent successfully - Order #{} confirmed for {}", orderId, customerName);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order confirmation notification interrupted for orderId={}", orderId, e);
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

            simulateEmailDelay();

            // TODO: Integrate with actual email service
            log.info("Order shipped notification sent - Order #{} has been shipped for {} (Tracking: {})",
                    orderId, customerName, trackingNumber);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order shipped notification interrupted for orderId={}", orderId, e);
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

            simulateEmailDelay();

            // TODO: Integrate with actual email service
            log.info("Order delivered notification sent - Order #{} has been delivered to {}", orderId, customerName);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order delivered notification interrupted for orderId={}", orderId, e);
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

            simulateEmailDelay();

            // TODO: Integrate with actual email service
            log.info("Welcome email sent to {} ({})", fullName, email);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Welcome email interrupted for user: {}", email, e);
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

            simulateEmailDelay();

            // TODO: Integrate with actual email service
            log.info("Password reset email sent to {} with token: {}", email, resetToken.substring(0, 8) + "...");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Password reset email interrupted for user: {}", email, e);
        } catch (Exception e) {
            log.error("Failed to send password reset email to user: {}", email, e);
        }
    }
}