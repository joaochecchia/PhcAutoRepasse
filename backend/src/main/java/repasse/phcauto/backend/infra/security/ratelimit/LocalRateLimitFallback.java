package repasse.phcauto.backend.infra.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Proteção degradada por instância, usada somente quando o Redis não responde. */
final class LocalRateLimitFallback {
    private final ConcurrentHashMap<String, Janela> janelas = new ConcurrentHashMap<>();
    private final AtomicLong operacoes = new AtomicLong();
    private final Clock clock;
    private final int maximoChaves;

    LocalRateLimitFallback(Clock clock, int maximoChaves) {
        if (maximoChaves < 100) throw new IllegalArgumentException("Fallback local exige ao menos 100 chaves");
        this.clock = clock;
        this.maximoChaves = maximoChaves;
    }

    void consumir(String chave, int limite, Duration janela) {
        long agora = clock.millis();
        long duracao = Math.max(1_000, janela.toMillis());
        if ((operacoes.incrementAndGet() & 255) == 0) removerExpiradas(agora);
        if (!janelas.containsKey(chave) && janelas.size() >= maximoChaves) {
            removerExpiradas(agora);
            if (janelas.size() >= maximoChaves) {
                throw new RateLimitExceededException(Math.max(1, janela.toSeconds()));
            }
        }
        Janela resultado = janelas.compute(chave, (ignorada, atual) -> {
            if (atual == null || atual.expiraEmMillis <= agora) return new Janela(1, agora + duracao);
            return new Janela(atual.quantidade + 1, atual.expiraEmMillis);
        });
        if (resultado.quantidade > limite) {
            long segundos = Math.max(1, (resultado.expiraEmMillis - agora + 999) / 1_000);
            throw new RateLimitExceededException(segundos);
        }
    }

    void limpar(String chave) { janelas.remove(chave); }

    private void removerExpiradas(long agora) {
        janelas.entrySet().removeIf(entry -> entry.getValue().expiraEmMillis <= agora);
    }

    private record Janela(int quantidade, long expiraEmMillis) { }
}
