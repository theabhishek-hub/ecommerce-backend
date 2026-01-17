package com.abhishek.ecommerce.seller.entity;

import com.abhishek.ecommerce.common.baseEntity.BaseEntity;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Seller entity represents a marketplace seller profile.
 * One-to-One relationship with User.
 * Manages seller lifecycle: PENDING -> APPROVED/REJECTED -> SUSPENDED.
 */
@Entity
@Table(name = "sellers", indexes = {
        @Index(name = "idx_seller_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_seller_status", columnList = "status"),
        @Index(name = "idx_seller_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "approvedByAdmin"})
@EqualsAndHashCode(exclude = {"user", "approvedByAdmin"}, callSuper = false)
public class Seller extends BaseEntity {

    /**
     * One-to-One mapping to User entity
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    /**
     * Current status of seller application
     * Values: REQUESTED, APPROVED, REJECTED, SUSPENDED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SellerStatus status = SellerStatus.REQUESTED;

    /**
     * Timestamp when seller was approved
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Admin user who approved/rejected the seller
     * Nullable if not yet approved/rejected
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_admin_id")
    private User approvedByAdmin;

    /**
     * Rejection or suspension reason
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * Business details for seller application
     */
    @Column(name = "business_name", length = 255)
    private String businessName;

    @Column(name = "business_description", columnDefinition = "TEXT")
    private String businessDescription;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "street_address", length = 255)
    private String streetAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc_code", length = 20)
    private String bankIfscCode;

    /**
     * Timestamp when seller submitted application details
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

}
