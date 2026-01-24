package com.abhishek.ecommerce.user.entity;

import com.abhishek.ecommerce.common.baseEntity.BaseEntity;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Optional entity to store seller business details and application information.
 * Has a one-to-one relationship with User entity.
 * Only created when a user applies to become a seller.
 */
@Entity
@Table(name = "seller_applications", indexes = {
        @Index(name = "idx_seller_app_user_id", columnList = "user_id"),
        @Index(name = "idx_seller_app_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user", callSuper = false)
public class SellerApplication extends BaseEntity {

    /**
     * One-to-one relationship with User entity
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // ======================== BUSINESS DETAILS ========================

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "business_type", length = 100)
    private String businessType; // e.g., INDIVIDUAL, PARTNERSHIP, COMPANY

    @Column(name = "business_description", length = 1000)
    private String businessDescription;

    // ======================== TAX INFORMATION ========================

    /**
     * PAN (Permanent Account Number) - India tax identifier
     */
    @Column(name = "pan", length = 50)
    private String pan;

    /**
     * GST (Goods and Services Tax) registration number
     */
    @Column(name = "gst_number", length = 50)
    private String gstNumber;

    /**
     * VAT or equivalent tax identifier for other countries
     */
    @Column(name = "tax_id", length = 100)
    private String taxId;

    // ======================== BUSINESS ADDRESS ========================

    @Column(name = "business_address_line1", length = 255)
    private String addressLine1;

    @Column(name = "business_address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    // ======================== CONTACT INFORMATION ========================

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // ======================== BANK INFORMATION ========================

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "account_holder_name", length = 255)
    private String accountHolderName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "swift_code", length = 20)
    private String swiftCode;

    // ======================== DOCUMENT INFORMATION ========================

    /**
     * URL or path to PAN document
     */
    @Column(name = "pan_document_url", length = 500)
    private String panDocumentUrl;

    /**
     * URL or path to GST certificate
     */
    @Column(name = "gst_document_url", length = 500)
    private String gstDocumentUrl;

    /**
     * URL or path to business registration/proof
     */
    @Column(name = "business_proof_url", length = 500)
    private String businessProofUrl;

    /**
     * URL or path to bank verification document
     */
    @Column(name = "bank_proof_url", length = 500)
    private String bankProofUrl;

    // ======================== APPLICATION STATUS ========================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SellerStatus status;

    @Column(name = "submission_date")
    private java.time.LocalDateTime submissionDate;

    @Column(name = "review_date")
    private java.time.LocalDateTime reviewDate;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "additional_notes", length = 1000)
    private String additionalNotes;

}
