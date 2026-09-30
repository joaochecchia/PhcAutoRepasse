package repasse.phcauto.backend.anuncios.internal.core.usecase;

import repasse.phcauto.backend.anuncios.internal.core.domain.BuscarAnunciosFiltro;
import repasse.phcauto.backend.anuncios.internal.core.domain.PaginaAnuncios;

public interface BuscarAnunciosUseCase {
    PaginaAnuncios execute(BuscarAnunciosFiltro filtro);
}
