package repasse.phcauto.backend.autenticacao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DispositivosConfiaveis {
    List<Dispositivo> listar(UUID usuarioId, String tokenAtual);
    void revogar(UUID usuarioId, UUID dispositivoId);
    void revogarTodos(UUID usuarioId);
    record Dispositivo(UUID id, String nome, Instant criadoEm, Instant ultimoUsoEm,
            Instant expiraEm, boolean dispositivoAtual) { }
}
