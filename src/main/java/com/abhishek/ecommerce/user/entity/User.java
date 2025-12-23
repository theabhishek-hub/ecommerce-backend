package com.abhishek.ecommerce.user.entity;

import com.abhishek.ecommerce.common.entity.Address;
import com.abhishek.ecommerce.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Store hashed password only (BCrypt).
     */
    @Column(nullable = false, length = 255)
    private String passwordHash;

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
}

