package com.abhishek.ecommerce.common.baseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter

public class Money {

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    protected Money()
    {

    }

    public Money(BigDecimal amount, String currency)
    {
        this.amount = amount;
        this.currency = currency;
    }


}

