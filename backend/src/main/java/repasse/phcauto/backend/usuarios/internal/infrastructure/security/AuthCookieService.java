package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieService {
    public static final String COOKIE_NAME = "PHC_AUTH";
    public static final String REFRESH_COOKIE_NAME = "PHC_REFRESH";
    private final boolean secure;

    public AuthCookieService(@Value("${AUTH_COOKIE_SECURE:false}") boolean secure) {
        this.secure = secure;
    }

    public String criar(String token, long expiresIn) {
        return cookie(token, Duration.ofSeconds(expiresIn)).toString();
    }

    public String remover() {
        return cookie(COOKIE_NAME, "", Duration.ZERO).toString();
    }

    public String criarRefresh(String token, long expiresIn) {
        return cookie(REFRESH_COOKIE_NAME, token, Duration.ofSeconds(expiresIn)).toString();
    }

    public String removerRefresh() { return cookie(REFRESH_COOKIE_NAME, "", Duration.ZERO).toString(); }

    private ResponseCookie cookie(String valor, Duration maxAge) {
        return cookie(COOKIE_NAME, valor, maxAge);
    }

    private ResponseCookie cookie(String nome, String valor, Duration maxAge) {
        return ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
