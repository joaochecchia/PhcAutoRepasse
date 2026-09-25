package repasse.phcauto.backend.usuarios.internal.core;

import java.util.UUID;

public interface ExcluirUsuarioGateway {
    UsuarioCompleto excluir(UUID usuarioId);
}
