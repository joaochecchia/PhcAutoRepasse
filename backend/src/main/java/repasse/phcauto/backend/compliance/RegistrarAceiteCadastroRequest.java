package repasse.phcauto.backend.compliance;

import java.time.Instant;
import java.util.UUID;

public record RegistrarAceiteCadastroRequest(
        UUID usuarioId,
        Boolean aceitouTermos,
        Instant aceiteEm,
        String versaoTermosUso,
        String versaoPoliticaPrivacidade,
        String enderecoRede) {
}
