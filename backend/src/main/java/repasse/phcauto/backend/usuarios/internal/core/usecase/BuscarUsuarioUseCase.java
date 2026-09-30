package repasse.phcauto.backend.usuarios.internal.core.usecase;

import java.util.UUID;

import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioCompleto;
public interface BuscarUsuarioUseCase {
    UsuarioCompleto execute(UUID usuarioId);
}
