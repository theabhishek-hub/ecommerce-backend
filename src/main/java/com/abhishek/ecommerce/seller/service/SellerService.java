package com.abhishek.ecommerce.seller.service;

import com.abhishek.ecommerce.seller.dto.response.SellerResponseDto;
import com.abhishek.ecommerce.shared.enums.SellerStatus;

import java.util.List;

public interface SellerService {

    /**
     * User applies to become a seller
     * Creates a new Seller entity with status = PENDING
     * Prevents duplicate applications
     */
    SellerResponseDto applyForSeller(Long userId);

    /**
     * Admin approves a seller application
     * Sets status = APPROVED
     * Assigns ROLE_SELLER to the user if not already present
     * Records admin and approval timestamp
     */
    SellerResponseDto approveSeller(Long sellerId, Long adminUserId);

    /**
     * Admin rejects a seller application
     * Sets status = REJECTED
     * Removes ROLE_SELLER from the user
     * Records admin, rejection timestamp, and optional reason
     */
    SellerResponseDto rejectSeller(Long sellerId, Long adminUserId, String rejectionReason);

    /**
     * Admin suspends an approved seller
     * Sets status = SUSPENDED
     * Removes ROLE_SELLER from the user (prevents dashboard access)
     */
    SellerResponseDto suspendSeller(Long sellerId, Long adminUserId, String suspensionReason);

    /**
     * Get seller profile by seller ID
     */
    SellerResponseDto getSellerById(Long sellerId);

    /**
     * Get seller profile by user ID
     * Returns null if user is not a seller
     */
    SellerResponseDto getSellerByUserId(Long userId);

    /**
     * Get all sellers with PENDING status (for admin review)
     */
    List<SellerResponseDto> getAllPendingSellers();

    /**
     * Get all sellers with specific status
     */
    List<SellerResponseDto> getSellersByStatus(SellerStatus status);

    /**
     * Check if user is an approved seller
     */
    boolean isApprovedSeller(Long userId);

    /**
     * Check if user has a seller profile (any status)
     */
    boolean isSellerApplicant(Long userId);

    /**
     * Count sellers by status
     */
    long countByStatus(SellerStatus status);

}
