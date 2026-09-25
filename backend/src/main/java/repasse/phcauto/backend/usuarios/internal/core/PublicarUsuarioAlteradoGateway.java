package repasse.phcauto.backend.usuarios.internal.core;

import repasse.phcauto.backend.domain.event.identidade.UsuarioAlterado;

public interface PublicarUsuarioAlteradoGateway {
    void publicar(UsuarioAlterado evento);
}
