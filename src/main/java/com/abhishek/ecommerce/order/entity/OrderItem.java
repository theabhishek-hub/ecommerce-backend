package com.abhishek.ecommerce.order.entity;

import com.abhishek.ecommerce.common.entity.BaseEntity;
import com.abhishek.ecommerce.common.entity.Money;
import com.abhishek.ecommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem extends BaseEntity {

    @ManyToOne
    private Order order;

    @ManyToOne
    private Product product;

    private Integer quantity;

    @Embedded
    private Money price;
}

