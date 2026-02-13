package com.example.pragmatic.application.auth.query;

import com.example.pragmatic.domain.auth.RefreshToken;
import com.example.pragmatic.domain.auth.RefreshTokenNotFoundException;
import com.example.pragmatic.infrastructure.persistence.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenQueryService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken getValidToken(final String token, final LocalDateTime now) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (refreshToken.isExpired(now)) {
            throw new RefreshTokenNotFoundException();
        }

        return refreshToken;
    }


}