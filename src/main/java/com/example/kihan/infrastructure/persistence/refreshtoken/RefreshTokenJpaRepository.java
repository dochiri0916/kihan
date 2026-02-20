package com.example.kihan.infrastructure.persistence.refreshtoken;

import com.example.kihan.domain.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {

    long deleteByExpiresAtBefore(LocalDateTime now);

    Optional<RefreshToken> findByToken(String token);

    long deleteByToken(String token);

    Optional<RefreshToken> findByUserId(Long userId);

}
