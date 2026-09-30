package repasse.phcauto.backend.usuarios.internal.core.usecase;

import java.util.UUID;

public interface ExcluirUsuarioUseCase {
    void execute(UUID usuarioId);
}
