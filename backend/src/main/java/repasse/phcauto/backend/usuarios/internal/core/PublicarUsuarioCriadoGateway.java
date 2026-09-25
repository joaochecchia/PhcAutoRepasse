package repasse.phcauto.backend.usuarios.internal.core;

import repasse.phcauto.backend.usuarios.UsuarioCriado;

public interface PublicarUsuarioCriadoGateway {
    void publicar(UsuarioCriado evento);
}
