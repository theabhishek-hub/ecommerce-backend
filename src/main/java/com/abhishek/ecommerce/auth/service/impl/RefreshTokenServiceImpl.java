package com.abhishek.ecommerce.auth.service.impl;

import com.abhishek.ecommerce.auth.entity.RefreshToken;
import com.abhishek.ecommerce.auth.repository.RefreshTokenRepository;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import com.abhishek.ecommerce.security.jwt.JwtUtil;
import com.abhishek.ecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public RefreshToken createOrReplaceRefreshToken(User user) {

        // 1️⃣ Generate token internally
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // 2️⃣ Calculate expiry internally
        Instant expiry = Instant.now()
                .plusSeconds(jwtUtil.getRefreshExpirationSeconds());

        // 3️⃣ Save / replace
        repository.deleteByUsername(user.getEmail());

        RefreshToken entity = new RefreshToken();
        entity.setUsername(user.getEmail());
        entity.setToken(refreshToken);
        entity.setExpiresAt(expiry);

        return repository.save(entity);
    }

    @Override
    @Transactional
    public RefreshToken validateAndGet(String token) {
        Optional<RefreshToken> opt = repository.findByToken(token);
        if (opt.isEmpty()) {
            throw new IllegalStateException("Invalid refresh token");
        }
        RefreshToken rt = opt.get();
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(rt);
            throw new IllegalStateException("Refresh token expired");
        }
        return rt;
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        repository.deleteByUsername(username);
    }
}

