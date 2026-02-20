package com.example.kihan.application.auth.command;

import com.example.kihan.domain.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RevokeTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public long execute(RevokeTokenCommand command) {
        return refreshTokenRepository.deleteByExpiresAtBefore(command.now());
    }

    @Transactional
    public long revokeByToken(String token) {
        return refreshTokenRepository.deleteByToken(token);
    }

}
