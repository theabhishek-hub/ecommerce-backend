package com.abhishek.ecommerce.auth.service;

import com.abhishek.ecommerce.auth.dto.AuthResponseDto;
import com.abhishek.ecommerce.auth.dto.LoginRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupResponseDto;

public interface AuthService {

    SignupResponseDto signup(SignupRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}

