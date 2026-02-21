package com.dochiri.kihan.infrastructure.security.jwt;

import com.dochiri.kihan.domain.user.UserRepository;
import com.dochiri.kihan.domain.user.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
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
        Claims claims = parseClaims(token);

        if (!jwtProvider.isAccessToken(claims)) {
            throw new BadCredentialsException("인증에 사용할 수 없는 토큰입니다.");
        }

        Long userId = jwtProvider.extractUserId(claims);
        UserRole role = userRepository.findByIdAndDeletedAtIsNull(userId).getRole();

        JwtPrincipal principal = new JwtPrincipal(userId, role);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }

    private Claims parseClaims(String token) {
        try {
            return jwtProvider.parseAndValidate(token);
        } catch (ExpiredJwtException exception) {
            throw new CredentialsExpiredException("액세스 토큰이 만료되었습니다.", exception);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BadCredentialsException("유효하지 않은 토큰입니다.", exception);
        }
    }

}
