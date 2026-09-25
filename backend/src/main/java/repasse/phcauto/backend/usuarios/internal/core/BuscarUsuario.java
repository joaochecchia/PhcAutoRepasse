package repasse.phcauto.backend.usuarios.internal.core;

import java.util.Objects;
import java.util.UUID;

public final class BuscarUsuario implements BuscarUsuarioUseCase {
    private final ConsultarUsuarioGateway usuarios;

    public BuscarUsuario(ConsultarUsuarioGateway usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Override public UsuarioCompleto execute(UUID usuarioId) {
        return usuarios.buscar(Objects.requireNonNull(usuarioId, "usuarioId"));
    }
}
