package repasse.phcauto.backend.usuarios.internal.core;

import java.util.UUID;

public interface ConsultarUsuarioGateway {
    UsuarioCompleto buscar(UUID usuarioId);
}
