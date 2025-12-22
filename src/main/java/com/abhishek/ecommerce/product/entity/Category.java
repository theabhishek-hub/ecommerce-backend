package com.abhishek.ecommerce.product.entity;

import com.abhishek.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category extends BaseEntity {

    private String name;

    /**
     * Supports category hierarchy (Furniture → Chairs).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Category parent;
}

