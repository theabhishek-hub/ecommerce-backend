package com.abhishek.ecommerce.product.entity;

import com.abhishek.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "brands")
@Getter
@Setter
public class Brand extends BaseEntity {

    private String name;
}

