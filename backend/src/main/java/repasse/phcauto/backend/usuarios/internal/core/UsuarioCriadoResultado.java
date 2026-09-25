package repasse.phcauto.backend.usuarios.internal.core;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record UsuarioCriadoResultado(UUID id, TipoPessoa tipoPessoa, String nome,
        String email, Instant criadoEm) { }
