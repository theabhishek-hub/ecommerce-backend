package com.abhishek.ecommerce.shared.enums;

/**
 * Seller status lifecycle:
 * - NOT_A_SELLER: User has not requested seller privileges (default)
 * - REQUESTED: User has applied but admin hasn't approved yet
 * - APPROVED: Admin has approved, user can access seller features with ROLE_SELLER
 * - REJECTED: Admin rejected the application
 * - SUSPENDED: Seller was active but admin suspended due to policy violation
 */
public enum SellerStatus {
    NOT_A_SELLER,
    REQUESTED,
    APPROVED,
    REJECTED,
    SUSPENDED
}
