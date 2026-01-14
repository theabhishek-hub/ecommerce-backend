package com.abhishek.ecommerce.common.baseEntity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
public class AuditMetadata {

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

