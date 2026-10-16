package repasse.phcauto.backend.autenticacao.internal.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class DeviceCookieService {
    private final boolean secure;
    private final Duration ttl;
    public DeviceCookieService(@Value("${AUTH_COOKIE_SECURE:false}") boolean secure,
            @Value("${app.security.trusted-device-ttl:30d}") Duration ttl) {
        this.secure=secure; this.ttl=ttl;
    }
    public String criar(String token) { return cookie(token, ttl).toString(); }
    public String remover() { return cookie("", Duration.ZERO).toString(); }
    public String name(){return secure ? "__Host-PHC_DEVICE" : "PHC_DEVICE";}
    public Duration ttl(){return ttl;}
    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(name(),value).httpOnly(true).secure(secure).sameSite("Lax")
                .path("/").maxAge(maxAge).build();
    }
}
