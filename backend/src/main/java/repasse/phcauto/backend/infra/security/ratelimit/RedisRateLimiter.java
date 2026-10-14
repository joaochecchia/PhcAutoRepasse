package repasse.phcauto.backend.infra.security.ratelimit;

import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Clock;
import java.util.HexFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisRateLimiter implements RateLimiter {
    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);
    private static final RedisScript<Long> CONSUMIR = RedisScript.of("""
            local atual = redis.call('GET', KEYS[1])
            if atual and tonumber(atual) >= tonumber(ARGV[1]) then return -1 end
            atual = redis.call('INCR', KEYS[1])
            if atual == 1 then redis.call('EXPIRE', KEYS[1], ARGV[2]) end
            return tonumber(ARGV[1]) - atual
            """, Long.class);

    private final StringRedisTemplate redis;
    private final MeterRegistry metrics;
    private final String prefixo;
    private final LocalRateLimitFallback fallback;

    public RedisRateLimiter(StringRedisTemplate redis, MeterRegistry metrics,
            Clock applicationClock,
            @Value("${app.rate-limit.key-prefix:phcauto:ratelimit:}") String prefixo,
            @Value("${app.rate-limit.fallback-max-keys:10000}") int fallbackMaxKeys) {
        this.redis = redis;
        this.metrics = metrics;
        this.prefixo = prefixo;
        this.fallback = new LocalRateLimitFallback(applicationClock, fallbackMaxKeys);
    }

    @Override
    public void consumir(String regra, String identificador, int limite, Duration janela) {
        if (limite < 1 || janela.isZero() || janela.isNegative()) return;
        try {
            Long restante = redis.execute(CONSUMIR, List.of(chave(regra, identificador)),
                    Integer.toString(limite), Long.toString(janela.toSeconds()));
            if (restante != null && restante < 0) {
                metrics.counter("phcauto.ratelimit.requests", "regra", regra, "resultado", "bloqueado").increment();
                throw new RateLimitExceededException(janela.toSeconds());
            }
            metrics.counter("phcauto.ratelimit.requests", "regra", regra, "resultado", "permitido").increment();
        } catch (RateLimitExceededException error) {
            throw error;
        } catch (RuntimeException error) {
            metrics.counter("phcauto.ratelimit.redis.errors", "regra", regra).increment();
            log.error("Rate limiting Redis indisponível para a regra {}; usando fallback local", regra, error);
            try {
                fallback.consumir(chave(regra, identificador), limite, janela);
                metrics.counter("phcauto.ratelimit.fallback.requests", "regra", regra,
                        "resultado", "permitido").increment();
            } catch (RateLimitExceededException bloqueio) {
                metrics.counter("phcauto.ratelimit.fallback.requests", "regra", regra,
                        "resultado", "bloqueado").increment();
                throw bloqueio;
            }
        }
    }

    @Override
    public void limpar(String regra, String identificador) {
        String chave = chave(regra, identificador);
        fallback.limpar(chave);
        try { redis.delete(chave); }
        catch (RuntimeException error) { log.warn("Não foi possível limpar rate limit da regra {}", regra, error); }
    }

    private String chave(String regra, String identificador) {
        return prefixo + regra + ":" + sha256(identificador == null ? "desconhecido" : identificador);
    }

    private static String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }
}
