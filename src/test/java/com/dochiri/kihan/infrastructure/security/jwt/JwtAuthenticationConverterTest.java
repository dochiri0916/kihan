package com.dochiri.kihan.infrastructure.security.jwt;

import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.domain.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationConverter 테스트")
class JwtAuthenticationConverterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Claims claims;

    @InjectMocks
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    @DisplayName("유효한 access 토큰이면 인증 객체를 생성한다")
    void shouldCreateAuthenticationWhenAccessTokenIsValid() {
        when(jwtProvider.parseAndValidate("access-token")).thenReturn(claims);
        when(jwtProvider.isAccessToken(claims)).thenReturn(true);
        when(jwtProvider.extractUserId(claims)).thenReturn(1L);

        User user = User.register("a@a.com", "pw", "alice");
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(user);

        UsernamePasswordAuthenticationToken authentication = jwtAuthenticationConverter.convert("access-token");

        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        assertEquals(1L, principal.userId());
        assertEquals(user.getRole(), principal.role());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.iterator().next().getAuthority().startsWith("ROLE_"));
    }

    @Test
    @DisplayName("access 카테고리가 아니면 예외를 던진다")
    void shouldThrowWhenTokenCategoryIsNotAccess() {
        when(jwtProvider.parseAndValidate("refresh-token")).thenReturn(claims);
        when(jwtProvider.isAccessToken(claims)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> jwtAuthenticationConverter.convert("refresh-token"));
    }

    @Test
    @DisplayName("만료된 토큰 파싱 시 CredentialsExpiredException으로 변환한다")
    void shouldConvertExpiredJwtToCredentialsExpiredException() {
        when(jwtProvider.parseAndValidate("expired"))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        assertThrows(CredentialsExpiredException.class, () -> jwtAuthenticationConverter.convert("expired"));
    }

    @Test
    @DisplayName("잘못된 토큰 파싱 시 BadCredentialsException으로 변환한다")
    void shouldConvertJwtExceptionToBadCredentialsException() {
        when(jwtProvider.parseAndValidate("invalid")).thenThrow(new JwtException("invalid"));

        assertThrows(BadCredentialsException.class, () -> jwtAuthenticationConverter.convert("invalid"));
    }
}
