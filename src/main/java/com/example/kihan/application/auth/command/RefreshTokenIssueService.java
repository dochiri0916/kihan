package com.example.kihan.application.auth.command;

import com.example.kihan.domain.auth.RefreshToken;
import com.example.kihan.infrastructure.persistence.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenIssueService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void issue(
            final String token,
            final Long userId,
            final LocalDateTime expiresAt
    ) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existingToken -> existingToken.update(token, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.issue(token, userId, expiresAt)
                        )
                );
    }

}