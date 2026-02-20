package com.example.kihan.domain.auth;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    RefreshToken findByToken(String token);

    Optional<RefreshToken> findByUserId(Long userId);

    long deleteByToken(String token);

    long deleteByExpiresAtBefore(LocalDateTime now);

}
