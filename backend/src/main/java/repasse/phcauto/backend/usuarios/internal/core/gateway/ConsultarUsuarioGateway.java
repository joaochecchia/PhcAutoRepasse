package repasse.phcauto.backend.usuarios.internal.core.gateway;

import java.util.UUID;

import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioCompleto;
public interface ConsultarUsuarioGateway {
    UsuarioCompleto buscar(UUID usuarioId);
}
