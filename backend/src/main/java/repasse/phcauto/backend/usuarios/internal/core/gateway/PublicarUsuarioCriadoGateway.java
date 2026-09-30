package repasse.phcauto.backend.usuarios.internal.core.gateway;

import repasse.phcauto.backend.usuarios.UsuarioCriado;

public interface PublicarUsuarioCriadoGateway {
    void publicar(UsuarioCriado evento);
}
