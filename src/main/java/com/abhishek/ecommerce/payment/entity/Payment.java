package com.abhishek.ecommerce.payment.entity;

import com.abhishek.ecommerce.common.entity.BaseEntity;
import com.abhishek.ecommerce.common.entity.Money;
import com.abhishek.ecommerce.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity {

    @OneToOne
    private Order order;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}

