package com.abhishek.ecommerce.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${config.email.shipping-team-email:shipping@ecommerce.com}")
    private String shippingTeamEmail;

    /**
     * Send order confirmation notification asynchronously
     * This is a side effect and should not affect business logic
     */
    @Async
    public void sendOrderConfirmation(Long orderId, String customerEmail, String customerName) {
        try {
            log.info("[NotificationService] Async task started - Sending order confirmation for orderId={} to email={}", orderId, customerEmail);

            // Send actual email
            emailService.sendOrderConfirmationEmail(customerEmail, customerName, orderId);
            log.info("[NotificationService] Order confirmation email sent successfully");

        } catch (Exception e) {
            log.error("[NotificationService] Failed to send order confirmation for orderId={}: {}", orderId, e.getMessage(), e);
            // Don't throw exception - this is a side effect, shouldn't affect business logic
        }
    }

    /**
     * Send order confirmed by seller notification asynchronously
     * Called when seller confirms an order (PAID -> CONFIRMED)
     */
    @Async
    public void sendOrderConfirmedNotification(Long orderId, String customerEmail, String customerName) {
        try {
            log.info("[NotificationService] Async task started - Sending order confirmed notification for orderId={} to email={}", orderId, customerEmail);

            // Send actual email - reuse confirmation email
            emailService.sendOrderConfirmationEmail(customerEmail, customerName, orderId);
            log.info("[NotificationService] Order confirmed notification email sent successfully");

        } catch (Exception e) {
            log.error("[NotificationService] Failed to send order confirmed notification for orderId={}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Send shipping confirmation to shipping department email asynchronously
     * Called when seller confirms an order - notifies shipping team to prepare for shipment
     */
    @Async
    public void sendShippingConfirmationToShippingTeam(Long orderId, String customerEmail, String customerName) {
        try {
            log.info("Sending shipping confirmation to shipping team for orderId={}", orderId);

            // Send email to configured shipping team email
            emailService.sendShippingConfirmationEmail(shippingTeamEmail, orderId, customerName);

        } catch (Exception e) {
            log.error("Failed to send shipping confirmation to shipping team for orderId={}", orderId, e);
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

    /**
     * Send seller approved notification asynchronously
     */
    @Async
    public void sendSellerApprovedNotification(Long userId, String email, String fullName) {
        try {
            log.info("Sending seller approved notification to: {}", email);
            // TODO: Implement seller approved email template
            log.debug("Seller {} approved - notification would be sent to {}", userId, email);
        } catch (Exception e) {
            log.error("Failed to send seller approved notification to user: {}", email, e);
        }
    }

    /**
     * Send seller rejected notification asynchronously
     */
    @Async
    public void sendSellerRejectedNotification(Long userId, String email, String fullName, String rejectionReason) {
        try {
            log.info("Sending seller rejected notification to: {}", email);
            // TODO: Implement seller rejected email template
            log.debug("Seller {} rejected with reason: {} - notification would be sent to {}", userId, rejectionReason, email);
        } catch (Exception e) {
            log.error("Failed to send seller rejected notification to user: {}", email, e);
        }
    }

    /**
     * Send seller suspended notification asynchronously
     */
    @Async
    public void sendSellerSuspendedNotification(Long userId, String email, String fullName, String suspensionReason) {
        try {
            log.info("Sending seller suspended notification to: {}", email);
            // TODO: Implement seller suspended email template
            log.debug("Seller {} suspended with reason: {} - notification would be sent to {}", userId, suspensionReason, email);
        } catch (Exception e) {
            log.error("Failed to send seller suspended notification to user: {}", email, e);
        }
    }
}