package repasse.phcauto.backend.usuarios.internal.core.gateway;

import java.time.Instant;
import java.util.UUID;

import repasse.phcauto.backend.usuarios.internal.core.domain.AlteracoesUsuario;
import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioCompleto;
public interface AtualizarUsuarioGateway {
    boolean existeEmailDeOutroUsuario(String email, UUID usuarioId);
    boolean existeDocumentoDeOutroUsuario(UsuarioCompleto atual, String documento);
    UsuarioCompleto atualizar(UUID usuarioId, AlteracoesUsuario alteracoes, Instant agora);
}
