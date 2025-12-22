package com.abhishek.ecommerce.product.entity;

import com.abhishek.ecommerce.common.entity.BaseEntity;
import com.abhishek.ecommerce.common.entity.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends BaseEntity {

    private String name;

    @Column(length = 2000)
    private String description;

    @Embedded
    private Money price;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Brand brand;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;
}

