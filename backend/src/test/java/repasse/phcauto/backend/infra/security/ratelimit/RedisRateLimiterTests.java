package repasse.phcauto.backend.infra.security.ratelimit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisRateLimiterTests {
    @Test
    void usaFallbackLocalEBloqueiaQuandoRedisFalha() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class),
                anyList(), any(Object[].class))).thenThrow(new IllegalStateException("Redis indisponível"));
        var metricas = new SimpleMeterRegistry();
        var limiter = new RedisRateLimiter(redis, metricas, Clock.systemUTC(), "teste:", 100);

        limiter.consumir("login", "ip:192.0.2.1", 1, Duration.ofMinutes(1));
        assertThrows(RateLimitExceededException.class,
                () -> limiter.consumir("login", "ip:192.0.2.1", 1, Duration.ofMinutes(1)));

        assertEquals(2.0, metricas.get("phcauto.ratelimit.redis.errors").counter().count());
        assertEquals(1.0, metricas.get("phcauto.ratelimit.fallback.requests")
                .tag("resultado", "bloqueado").counter().count());
    }
}
