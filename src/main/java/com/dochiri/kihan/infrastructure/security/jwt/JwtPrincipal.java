package com.dochiri.kihan.infrastructure.security.jwt;

import com.dochiri.kihan.domain.user.UserRole;

public record JwtPrincipal(
        Long userId,
        UserRole role
) {
}
