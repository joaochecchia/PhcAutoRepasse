package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieService {
    public static final String COOKIE_NAME = "PHC_AUTH";
    private final boolean secure;

    public AuthCookieService(@Value("${AUTH_COOKIE_SECURE:false}") boolean secure) {
        this.secure = secure;
    }

    public String criar(String token, long expiresIn) {
        return cookie(token, Duration.ofSeconds(expiresIn)).toString();
    }

    public String remover() {
        return cookie("", Duration.ZERO).toString();
    }

    private ResponseCookie cookie(String valor, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
