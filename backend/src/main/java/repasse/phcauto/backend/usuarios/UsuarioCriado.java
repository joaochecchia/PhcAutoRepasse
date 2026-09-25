package repasse.phcauto.backend.usuarios;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

/** Fato público sem credenciais ou documentos pessoais. */
public record UsuarioCriado(UUID eventId, UUID usuarioId, TipoPessoa tipoPessoa, Instant ocorridoEm) {
    public UsuarioCriado {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(usuarioId);
        Objects.requireNonNull(tipoPessoa);
        Objects.requireNonNull(ocorridoEm);
    }
}
