package com.example.kihan.infrastructure.security.cookie;

import com.example.kihan.infrastructure.config.properties.CookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
        StringBuilder cookie = new StringBuilder();

        cookie.append(name).append('=').append(value).append("; ");
        cookie.append("Path=").append(cookieProperties.path()).append("; ");
        cookie.append("Max-Age=").append(maxAge).append("; ");

        appendIfPresent(cookie, "Domain", cookieProperties.domain());

        if (cookieProperties.httpOnly()) {
            cookie.append("HttpOnly; ");
        }

        if (cookieProperties.secure()) {
            cookie.append("Secure; ");
        }

        appendIfPresent(cookie, "SameSite", cookieProperties.sameSite());

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void appendIfPresent(StringBuilder cookie, String key, String value) {
        if (value != null && !value.isBlank()) {
            cookie.append(key).append('=').append(value).append("; ");
        }
    }

}