package com.abhishek.ecommerce.user.entity;

import com.abhishek.ecommerce.common.entity.Address;
import com.abhishek.ecommerce.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
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
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private Role role = Role.ROLE_USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider provider; // e.g., LOCAL, GOOGLE

    // Account security fields
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private java.time.LocalDateTime lockedUntil;

}