package com.example.kihan.application.auth.query;

import com.example.kihan.domain.auth.RefreshToken;
import com.example.kihan.domain.auth.RefreshTokenNotFoundException;
import com.example.kihan.infrastructure.persistence.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefreshTokenQueryService implements RefreshTokenLoader {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken loadValidToken(String token, LocalDateTime now) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (refreshToken.isExpired(now)) {
            throw new RefreshTokenNotFoundException();
        }

        return refreshToken;
    }

}