package repasse.phcauto.backend.compliance.internal.core.domain;

import java.time.Instant;
import java.util.UUID;

public record AceiteTermos(
        UUID id,
        UUID usuarioId,
        Instant aceiteEm,
        String versaoTermosUso,
        String versaoPoliticaPrivacidade,
        String enderecoRede,
        Instant registradoEm) {
}
