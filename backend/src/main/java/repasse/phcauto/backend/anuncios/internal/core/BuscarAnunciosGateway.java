package repasse.phcauto.backend.anuncios.internal.core;

public interface BuscarAnunciosGateway {
    PaginaAnuncios buscar(BuscarAnunciosFiltro filtro);
}
