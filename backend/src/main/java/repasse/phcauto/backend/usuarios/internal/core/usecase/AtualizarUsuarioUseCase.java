package repasse.phcauto.backend.usuarios.internal.core.usecase;

import java.util.UUID;
import repasse.phcauto.backend.usuarios.internal.core.domain.AtualizarUsuarioCommand;

import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioCompleto;
public interface AtualizarUsuarioUseCase {
    UsuarioCompleto execute(UUID usuarioId, AtualizarUsuarioCommand request);
}
