package repasse.phcauto.backend.usuarios;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record UsuarioResponse(UUID id, TipoPessoa tipoPessoa, String nome,
        String email, Instant criadoEm) { }
