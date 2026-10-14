package repasse.phcauto.backend.usuarios.internal.core.gateway;

import java.time.Instant;
import java.util.UUID;

public interface RevogarSessoesGateway {
    void revogarTodas(UUID usuarioId, Instant agora);
}
