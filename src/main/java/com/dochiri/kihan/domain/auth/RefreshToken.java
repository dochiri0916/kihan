package com.dochiri.kihan.domain.auth;

import com.dochiri.kihan.domain.BaseEntity;
import com.dochiri.kihan.domain.auth.exception.ExpiredRefreshTokenException;
import com.dochiri.kihan.domain.auth.exception.InvalidRefreshTokenException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.util.Objects.*;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static RefreshToken issue(String token, Long userId, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.token = requireNonNull(token);
        refreshToken.userId = requireNonNull(userId);
        refreshToken.expiresAt = requireNonNull(expiresAt);
        return refreshToken;
    }

    public void verifyNotExpired(LocalDateTime now) {
        if (now.isAfter(expiresAt)) {
            throw new ExpiredRefreshTokenException();
        }
    }

    public void verifyOwnership(Long userId) {
        if (!this.userId.equals(userId)) {
            throw InvalidRefreshTokenException.ownerMismatch();
        }
    }

    public void rotate(String newToken, LocalDateTime newExpiresAt) {
        this.token = requireNonNull(newToken);
        this.expiresAt = requireNonNull(newExpiresAt);
    }

}