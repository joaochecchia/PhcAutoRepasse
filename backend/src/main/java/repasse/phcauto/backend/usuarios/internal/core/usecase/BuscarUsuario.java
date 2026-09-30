package repasse.phcauto.backend.usuarios.internal.core.usecase;

import java.util.Objects;
import java.util.UUID;

import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioCompleto;
import repasse.phcauto.backend.usuarios.internal.core.gateway.ConsultarUsuarioGateway;
public final class BuscarUsuario implements BuscarUsuarioUseCase {
    private final ConsultarUsuarioGateway usuarios;

    public BuscarUsuario(ConsultarUsuarioGateway usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Override public UsuarioCompleto execute(UUID usuarioId) {
        return usuarios.buscar(Objects.requireNonNull(usuarioId, "usuarioId"));
    }
}
