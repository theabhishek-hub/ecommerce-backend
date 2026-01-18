package com.abhishek.ecommerce.user.entity;

import com.abhishek.ecommerce.common.baseEntity.Address;
import com.abhishek.ecommerce.common.baseEntity.BaseEntity;
import com.abhishek.ecommerce.shared.enums.AuthProvider;
import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_seller_status", columnList = "seller_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "addresses")
@EqualsAndHashCode(exclude = "addresses", callSuper = false)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Store hashed password only bcrypt
     */
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * One user can have multiple addresses.
     */
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Address> addresses = new ArrayList<>();

    // Added for security
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider provider; // e.g., LOCAL, GOOGLE

    // Account security fields
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private java.time.LocalDateTime lockedUntil;

    // ======================== SELLER FIELDS ========================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SellerStatus sellerStatus = SellerStatus.NOT_A_SELLER;

    @Column(name = "seller_requested_at")
    private java.time.LocalDateTime sellerRequestedAt;

    @Column(name = "seller_approved_at")
    private java.time.LocalDateTime sellerApprovedAt;

}