package com.example.kihan.domain.auth;

import com.example.kihan.domain.BaseEntity;
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

    private String token;

    private Long userId;

    private LocalDateTime expiresAt;

    public static RefreshToken issue(
            final String token,
            final Long userId,
            final LocalDateTime expiresAt
    ) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.token = requireNonNull(token);
        refreshToken.userId = requireNonNull(userId);
        refreshToken.expiresAt = requireNonNull(expiresAt);
        return refreshToken;
    }

    public void update(final String token, final LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(final LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public boolean isOwnedBy(final Long userId) {
        return this.userId.equals(userId);
    }

}