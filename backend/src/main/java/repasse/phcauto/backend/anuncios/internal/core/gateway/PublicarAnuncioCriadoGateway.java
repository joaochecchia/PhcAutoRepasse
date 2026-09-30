package repasse.phcauto.backend.anuncios.internal.core.gateway;

import repasse.phcauto.backend.anuncios.AnuncioCriado;

public interface PublicarAnuncioCriadoGateway {
    void publicar(AnuncioCriado evento);
}
