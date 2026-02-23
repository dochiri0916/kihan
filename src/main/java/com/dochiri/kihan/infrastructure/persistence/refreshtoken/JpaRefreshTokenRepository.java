package com.dochiri.kihan.infrastructure.persistence.refreshtoken;

import com.dochiri.kihan.domain.auth.RefreshToken;
import com.dochiri.kihan.domain.auth.exception.RefreshTokenNotFoundException;
import com.dochiri.kihan.domain.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return jpaRepository.save(refreshToken);
    }

    @Override
    public RefreshToken findByToken(String token) {
        return jpaRepository.findByToken(token)
                .orElseThrow(RefreshTokenNotFoundException::new);
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public long deleteByToken(String token) {
        return jpaRepository.deleteByToken(token);
    }

    @Override
    public long deleteByExpiresAtBefore(LocalDateTime now) {
        return jpaRepository.deleteByExpiresAtBefore(now);
    }

}
