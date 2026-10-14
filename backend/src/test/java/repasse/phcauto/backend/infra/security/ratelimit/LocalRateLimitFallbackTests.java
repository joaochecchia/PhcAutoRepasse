package repasse.phcauto.backend.infra.security.ratelimit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class LocalRateLimitFallbackTests {
    @Test
    void bloqueiaAoUltrapassarLimiteERenovaDepoisDaJanela() {
        var clock = new MutableClock();
        var fallback = new LocalRateLimitFallback(clock, 100);

        fallback.consumir("login:ip", 2, Duration.ofMinutes(1));
        fallback.consumir("login:ip", 2, Duration.ofMinutes(1));
        var bloqueio = assertThrows(RateLimitExceededException.class,
                () -> fallback.consumir("login:ip", 2, Duration.ofMinutes(1)));
        assertEquals(60, bloqueio.retryAfterSeconds());

        clock.avancar(Duration.ofMinutes(1));
        assertDoesNotThrow(() -> fallback.consumir("login:ip", 2, Duration.ofMinutes(1)));
    }

    @Test
    void limitaQuantidadeDeChavesParaEvitarExaustaoDeMemoria() {
        var fallback = new LocalRateLimitFallback(new MutableClock(), 100);
        for (int i = 0; i < 100; i++) fallback.consumir("chave-" + i, 10, Duration.ofHours(1));

        assertThrows(RateLimitExceededException.class,
                () -> fallback.consumir("chave-excedente", 10, Duration.ofHours(1)));
    }

    private static final class MutableClock extends Clock {
        private Instant agora = Instant.parse("2026-10-14T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
        void avancar(Duration duracao) { agora = agora.plus(duracao); }
    }
}
