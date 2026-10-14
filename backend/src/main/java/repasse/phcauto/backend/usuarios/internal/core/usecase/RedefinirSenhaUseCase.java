package repasse.phcauto.backend.usuarios.internal.core.usecase;

import java.util.UUID;
import repasse.phcauto.backend.usuarios.internal.core.domain.RedefinirSenhaCommand;

public interface RedefinirSenhaUseCase {
    void execute(UUID usuarioId, RedefinirSenhaCommand command);
}
