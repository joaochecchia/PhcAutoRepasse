package repasse.phcauto.backend.anuncios.internal.core.gateway;

import repasse.phcauto.backend.anuncios.internal.core.domain.BuscarAnunciosFiltro;
import repasse.phcauto.backend.anuncios.internal.core.domain.PaginaAnuncios;

public interface BuscarAnunciosGateway {
    PaginaAnuncios buscar(BuscarAnunciosFiltro filtro);
}
