package repasse.phcauto.backend.usuarios.internal.core;

import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public interface UsuarioGateway {
    boolean existeEmail(String email);
    boolean existeDocumento(TipoPessoa tipoPessoa, String documento);
    void salvarCadastro(UUID usuarioId, DadosNovoUsuario dados, Instant agora);
}
