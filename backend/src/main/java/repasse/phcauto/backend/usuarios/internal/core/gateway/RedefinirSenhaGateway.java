package repasse.phcauto.backend.usuarios.internal.core.gateway;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public interface RedefinirSenhaGateway {
    TipoPessoa redefinir(UUID usuarioId, String senhaHash, Instant atualizadoEm);
}
