package repasse.phcauto.backend.usuarios.internal.core;

import java.util.UUID;

public interface BuscarUsuarioUseCase {
    UsuarioCompleto execute(UUID usuarioId);
}
