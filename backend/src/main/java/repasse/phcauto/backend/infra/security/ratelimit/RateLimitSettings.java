package repasse.phcauto.backend.infra.security.ratelimit;

import java.time.Duration;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class RateLimitSettings {
    private final Environment environment;
    public RateLimitSettings(Environment environment) { this.environment = environment; }
    public Regra regra(String nome, int limite, Duration janela) {
        String valorJanela = environment.getProperty("app.rate-limit." + nome + ".window");
        return new Regra(nome,
                environment.getProperty("app.rate-limit." + nome + ".limit", Integer.class, limite),
                valorJanela == null ? janela : DurationStyle.detectAndParse(valorJanela));
    }
    public record Regra(String nome, int limite, Duration janela) { }
}
