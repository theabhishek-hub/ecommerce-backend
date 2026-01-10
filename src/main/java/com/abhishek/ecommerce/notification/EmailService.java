package com.abhishek.ecommerce.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email service for sending notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Send a simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            // Don't throw exception - email failure shouldn't break business logic
        }
    }

    /**
     * Send HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Password Reset Request";
        String htmlContent = String.format("""
            <html>
            <body>
                <h2>Password Reset</h2>
                <p>You have requested to reset your password.</p>
                <p>Use the following token to reset your password:</p>
                <h3>%s</h3>
                <p>This token will expire in 15 minutes.</p>
                <p>If you didn't request this, please ignore this email.</p>
                <br>
                <p>Best regards,<br>Ecommerce Team</p>
            </body>
            </html>
            """, resetToken);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send welcome email to new user
     */
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "Welcome to Our Ecommerce Platform!";
        String htmlContent = String.format("""
            <html>
            <body>
                <h2>Welcome %s!</h2>
                <p>Thank you for joining our ecommerce platform.</p>
                <p>You can now:</p>
                <ul>
                    <li>Browse our products</li>
                    <li>Add items to your cart</li>
                    <li>Place orders securely</li>
                    <li>Track your order history</li>
                </ul>
                <p>Happy shopping!</p>
                <br>
                <p>Best regards,<br>Ecommerce Team</p>
            </body>
            </html>
            """, fullName);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmationEmail(String to, String customerName, Long orderId) {
        String subject = String.format("Order Confirmation - Order #%d", orderId);
        String htmlContent = String.format("""
            <html>
            <body>
                <h2>Order Confirmation</h2>
                <p>Dear %s,</p>
                <p>Thank you for your order! Your order #%d has been successfully placed.</p>
                <p>You will receive updates on your order status via email.</p>
                <p>Order Details:</p>
                <ul>
                    <li>Order ID: %d</li>
                    <li>Status: Confirmed</li>
                </ul>
                <br>
                <p>Best regards,<br>Ecommerce Team</p>
            </body>
            </html>
            """, customerName, orderId, orderId);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send order shipped email
     */
    public void sendOrderShippedEmail(String to, String customerName, Long orderId, String trackingNumber) {
        String subject = String.format("Order Shipped - Order #%d", orderId);
        String htmlContent = String.format("""
            <html>
            <body>
                <h2>Order Shipped</h2>
                <p>Dear %s,</p>
                <p>Great news! Your order #%d has been shipped.</p>
                <p>Tracking Number: <strong>%s</strong></p>
                <p>You can track your package using the tracking number above.</p>
                <br>
                <p>Best regards,<br>Ecommerce Team</p>
            </body>
            </html>
            """, customerName, orderId, trackingNumber);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send order delivered email
     */
    public void sendOrderDeliveredEmail(String to, String customerName, Long orderId) {
        String subject = String.format("Order Delivered - Order #%d", orderId);
        String htmlContent = String.format("""
            <html>
            <body>
                <h2>Order Delivered</h2>
                <p>Dear %s,</p>
                <p>Your order #%d has been successfully delivered.</p>
                <p>Thank you for shopping with us!</p>
                <p>We hope to see you again soon.</p>
                <br>
                <p>Best regards,<br>Ecommerce Team</p>
            </body>
            </html>
            """, customerName, orderId);

        sendHtmlEmail(to, subject, htmlContent);
    }
}