package repasse.phcauto.backend.usuarios.internal.core.gateway;

import repasse.phcauto.backend.usuarios.internal.core.domain.UsuarioAutenticado;

public interface AutenticacaoGateway {
    UsuarioAutenticado autenticar(String email, String senha);
}
