package repasse.phcauto.backend.usuarios;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;
import repasse.phcauto.backend.domain.model.identidade.PapelUsuario;

public record UsuarioResponse(UUID id, TipoPessoa tipoPessoa, PapelUsuario papel, String nome,
        String email, Instant criadoEm) { }
