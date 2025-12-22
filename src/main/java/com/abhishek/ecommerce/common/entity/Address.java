package com.abhishek.ecommerce.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
public class Address extends BaseEntity {

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}

