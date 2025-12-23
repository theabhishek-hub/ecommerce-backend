package com.abhishek.ecommerce.cart.entity;

import com.abhishek.ecommerce.common.entity.BaseEntity;
import com.abhishek.ecommerce.common.entity.Money;
import com.abhishek.ecommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"cart_id", "product_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Embedded
    private Money price;   // snapshot from Product.price

    @Column(nullable = false)
    private Integer quantity;
}


