package com.dochiri.pragmatic.infrastructure.security.jwt;

import com.dochiri.pragmatic.domain.user.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public UsernamePasswordAuthenticationToken convert(String token) {
        Claims claims = jwtProvider.parseAndValidate(token);

        if (!jwtProvider.isAccessToken(claims)) {
            throw new BadCredentialsException("인증에 사용할 수 없는 토큰입니다.");
        }

        Long userId = jwtProvider.extractUserId(claims);
        String role = jwtProvider.extractRole(claims);

        userRepository.findByIdAndDeletedAtIsNull(userId);

        JwtPrincipal principal = new JwtPrincipal(userId, role);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

}