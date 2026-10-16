package repasse.phcauto.backend.autenticacao.internal.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationCookieService {
    private final boolean secure;
    public AuthenticationCookieService(@Value("${AUTH_COOKIE_SECURE:false}") boolean secure){this.secure=secure;}
    public String access(String token,long ttl){return cookie("PHC_AUTH",token,ttl).toString();}
    public String refresh(String token,long ttl){return cookie("PHC_REFRESH",token,ttl).toString();}
    private ResponseCookie cookie(String name,String value,long ttl){return ResponseCookie.from(name,value)
            .httpOnly(true).secure(secure).sameSite("Lax").path("/").maxAge(Duration.ofSeconds(ttl)).build();}
}
