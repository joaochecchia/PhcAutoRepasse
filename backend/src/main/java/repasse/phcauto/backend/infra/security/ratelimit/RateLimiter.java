package repasse.phcauto.backend.infra.security.ratelimit;

import java.time.Duration;

public interface RateLimiter {
    void consumir(String regra, String identificador, int limite, Duration janela);
    void limpar(String regra, String identificador);
}
