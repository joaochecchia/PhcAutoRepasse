package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.time.Duration;
import java.util.Locale;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import repasse.phcauto.backend.infra.security.ratelimit.RateLimiter;

@Component
public class LoginAttemptLimiter {
    private static final String REGRA = "tentativas-senha";
    private final RateLimiter limiter;
    private final int limite;
    private final Duration janela;
    public LoginAttemptLimiter(RateLimiter limiter, Environment environment) {
        this.limiter = limiter;
        this.limite = environment.getProperty("app.rate-limit.tentativas-senha.limit", Integer.class, 5);
        this.janela = environment.getProperty("app.rate-limit.tentativas-senha.window", Duration.class,
                Duration.ofMinutes(15));
    }
    public void antesDeAutenticar(String email) { limiter.consumir(REGRA, normalizar(email), limite, janela); }
    public void autenticacaoBemSucedida(String email) { limiter.limpar(REGRA, normalizar(email)); }
    private static String normalizar(String email) {
        return email == null ? "ausente" : email.trim().toLowerCase(Locale.ROOT);
    }
}
