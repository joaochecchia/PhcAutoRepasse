package repasse.phcauto.backend.usuarios.internal.core;

import java.time.Instant;
import java.util.UUID;

public interface AtualizarUsuarioGateway {
    boolean existeEmailDeOutroUsuario(String email, UUID usuarioId);
    boolean existeDocumentoDeOutroUsuario(UsuarioCompleto atual, String documento);
    UsuarioCompleto atualizar(UUID usuarioId, AlteracoesUsuario alteracoes, Instant agora);
}
