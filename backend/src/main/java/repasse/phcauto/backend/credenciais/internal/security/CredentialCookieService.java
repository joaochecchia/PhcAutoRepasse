package repasse.phcauto.backend.credenciais.internal.security;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CredentialCookieService {
    private final boolean secure;

    public CredentialCookieService(@Value("${AUTH_COOKIE_SECURE:false}") boolean secure) {
        this.secure = secure;
    }

    public List<String> removerTodos() {
        return List.of(remover("PHC_AUTH", "/"), remover("PHC_REFRESH", "/"),
                remover(secure ? "__Host-PHC_DEVICE" : "PHC_DEVICE", "/"));
    }

    private String remover(String nome, String path) {
        return ResponseCookie.from(nome, "").httpOnly(true).secure(secure).sameSite("Lax")
                .path(path).maxAge(Duration.ZERO).build().toString();
    }
}
