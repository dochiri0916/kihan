package com.dochiri.kihan.infrastructure.security.audit;

import com.dochiri.kihan.domain.user.UserRole;
import com.dochiri.kihan.infrastructure.security.jwt.JwtPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("AuditorAwareImpl 테스트")
class AuditorAwareImplTest {

    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증 정보가 없으면 SYSTEM을 반환한다")
    void shouldReturnSystemWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        String auditor = auditorAware.getCurrentAuditor().orElseThrow();

        assertEquals("SYSTEM", auditor);
    }

    @Test
    @DisplayName("익명 인증이면 SYSTEM을 반환한다")
    void shouldReturnSystemWhenAuthenticationIsAnonymous() {
        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String auditor = auditorAware.getCurrentAuditor().orElseThrow();

        assertEquals("SYSTEM", auditor);
    }

    @Test
    @DisplayName("JwtPrincipal 인증이면 userId를 문자열로 반환한다")
    void shouldReturnUserIdWhenPrincipalIsJwtPrincipal() {
        JwtPrincipal principal = new JwtPrincipal(77L, UserRole.USER);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String auditor = auditorAware.getCurrentAuditor().orElseThrow();

        assertEquals("77", auditor);
    }

    @Test
    @DisplayName("JwtPrincipal이 아닌 인증 주체면 SYSTEM을 반환한다")
    void shouldReturnSystemWhenPrincipalIsNotJwtPrincipal() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("plain-principal", null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String auditor = auditorAware.getCurrentAuditor().orElseThrow();

        assertEquals("SYSTEM", auditor);
    }
}
