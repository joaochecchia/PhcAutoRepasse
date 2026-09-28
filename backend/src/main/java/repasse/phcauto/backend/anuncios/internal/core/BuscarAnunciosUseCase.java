package repasse.phcauto.backend.anuncios.internal.core;

public interface BuscarAnunciosUseCase {
    PaginaAnuncios execute(BuscarAnunciosFiltro filtro);
}
