package repasse.phcauto.backend.usuarios.internal.core.gateway;

import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;

public interface PublicarUsuarioAlteradoGateway {
    void publicar(UsuarioAlterado evento);
}
