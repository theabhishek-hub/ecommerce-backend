package com.abhishek.ecommerce.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthProvider
{
    LOCAL,
    GOOGLE
}
