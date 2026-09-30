package repasse.phcauto.backend.usuarios.internal.core.usecase;

import repasse.phcauto.backend.usuarios.internal.core.domain.CriarUsuarioCommand;
import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioCriadoResultado;

public interface CriarUsuarioUseCase {
    UsuarioCriadoResultado execute(CriarUsuarioCommand command);
}
