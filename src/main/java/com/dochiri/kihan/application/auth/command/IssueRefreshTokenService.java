package com.dochiri.kihan.application.auth.command;

import com.dochiri.kihan.domain.auth.RefreshToken;
import com.dochiri.kihan.domain.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(IssueRefreshTokenCommand command) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(command.userId())
                .orElseGet(() ->
                        RefreshToken.issue(
                                command.token(),
                                command.userId(),
                                command.expiresAt()
                        )
                );

        refreshToken.rotate(command.token(), command.expiresAt());

        refreshTokenRepository.save(refreshToken);
    }

}