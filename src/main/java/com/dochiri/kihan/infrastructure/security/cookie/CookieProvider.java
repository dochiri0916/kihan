package com.dochiri.kihan.infrastructure.security.cookie;

import com.dochiri.kihan.infrastructure.config.properties.CookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieProvider {

    private final CookieProperties cookieProperties;

    public void addRefreshToken(HttpServletResponse response, String refreshToken) {
        setCookie(
                response,
                cookieProperties.refreshTokenName(),
                refreshToken,
                cookieProperties.maxAge()
        );
    }

    public void deleteRefreshToken(HttpServletResponse response) {
        setCookie(
                response,
                cookieProperties.refreshTokenName(),
                "",
                0
        );
    }

    private void setCookie(
            HttpServletResponse response,
            String name,
            String value,
            long maxAge
    ) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                .from(name, value)
                .path(cookieProperties.path())
                .maxAge(maxAge)
                .httpOnly(cookieProperties.httpOnly())
                .secure(cookieProperties.secure());

        if (cookieProperties.domain() != null && !cookieProperties.domain().isBlank()) {
            cookieBuilder.domain(cookieProperties.domain());
        }

        if (cookieProperties.sameSite() != null && !cookieProperties.sameSite().isBlank()) {
            cookieBuilder.sameSite(cookieProperties.sameSite());
        }

        response.addHeader("Set-Cookie", cookieBuilder.build().toString());
    }

}
