package com.abhishek.ecommerce.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String mailFromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFromAddress);
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
            log.info("[EmailService] Starting to send HTML email to: {} with subject: {}", to, subject);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFromAddress);
            log.info("[EmailService] Set from address: {}", mailFromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML

            log.info("[EmailService] Prepared message, about to send via JavaMailSender to: {}", to);
            mailSender.send(message);
            log.info("[EmailService] HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("[EmailService] FAILED to send HTML email to {}: {}", to, e.getMessage(), e);
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

    /**
     * Send shipping confirmation email to shipping team
     * Called when order is confirmed by seller - notifies shipping department to prepare shipment
     */
    public void sendShippingConfirmationEmail(String to, Long orderId, String customerName) {
        String subject = String.format("⚠️ NEW ORDER TO SHIP - Order #%d", orderId);
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 20px auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                    <h2 style="color: #d9534f; border-bottom: 2px solid #d9534f; padding-bottom: 10px;">🚚 ORDER READY FOR SHIPMENT</h2>
                    <p style="font-size: 16px; color: #333;">Shipping Team,</p>
                    
                    <p style="font-size: 14px; color: #666;">
                        A new order has been confirmed and is ready for shipment preparation.
                    </p>
                    
                    <div style="background-color: #f0f0f0; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #333; margin-top: 0;">Order Details:</h3>
                        <p style="margin: 8px 0;"><strong>Order ID:</strong> <span style="color: #d9534f; font-weight: bold;">#%d</span></p>
                        <p style="margin: 8px 0;"><strong>Customer Name:</strong> %s</p>
                        <p style="margin: 8px 0;"><strong>Status:</strong> <span style="background-color: #5cb85c; color: white; padding: 2px 8px; border-radius: 3px;">CONFIRMED</span></p>
                    </div>
                    
                    <p style="font-size: 14px; color: #666;">
                        Please prepare this order for shipment immediately.
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-bottom: 5px;">
                        <strong>Next Steps:</strong>
                    </p>
                    <ul style="font-size: 14px; color: #666;">
                        <li>Pick and verify items from warehouse</li>
                        <li>Pack securely with appropriate materials</li>
                        <li>Generate shipping label</li>
                        <li>Update tracking information</li>
                        <li>Hand over to courier partner</li>
                    </ul>
                    
                    <div style="border-top: 1px solid #ddd; padding-top: 15px; margin-top: 20px;">
                        <p style="font-size: 12px; color: #999; margin: 0;">This is an automated notification. Please do not reply to this email.</p>
                        <p style="font-size: 12px; color: #999; margin: 5px 0 0 0;">For support, contact the admin dashboard.</p>
                    </div>
                </div>
            </body>
            </html>
            """, orderId, customerName);

        sendHtmlEmail(to, subject, htmlContent);
    }
}