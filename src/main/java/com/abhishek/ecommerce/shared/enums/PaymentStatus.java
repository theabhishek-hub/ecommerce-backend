package com.abhishek.ecommerce.shared.enums;

public enum PaymentStatus {
    PENDING,      // Payment awaiting confirmation
    CONFIRMED,    // Payment confirmed (admin confirmed for COD, or Razorpay verified for online)
    SUCCESS,      // Payment successful (synonymous with CONFIRMED for some flows)
    FAILED,       // Payment failed
    REFUNDED      // Payment refunded
}



