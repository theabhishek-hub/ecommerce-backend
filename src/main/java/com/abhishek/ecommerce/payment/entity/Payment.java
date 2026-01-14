package com.abhishek.ecommerce.payment.entity;

import com.abhishek.ecommerce.common.baseEntity.BaseEntity;
import com.abhishek.ecommerce.common.baseEntity.Money;
import com.abhishek.ecommerce.shared.enums.PaymentStatus;
import com.abhishek.ecommerce.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "amount_currency", length = 3))
    })
    private Money amount;

    // Only for ONLINE payments (null for COD)
    @Column(name = "transaction_id")
    private String transactionId;

    
}


