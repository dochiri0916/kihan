package com.example.kihan.application.auth.command;

import com.example.kihan.infrastructure.persistence.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RevokeTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public long revokeExpiredTokens(final LocalDateTime now) {
        return refreshTokenRepository.deleteByExpiresAtBefore(now);
    }

}