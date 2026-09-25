package repasse.phcauto.backend.usuarios.internal.core;

import java.util.UUID;
import repasse.phcauto.backend.usuarios.request.AtualizarUsuarioRequest;

public interface AtualizarUsuarioUseCase {
    UsuarioCompleto execute(UUID usuarioId, AtualizarUsuarioRequest request);
}
