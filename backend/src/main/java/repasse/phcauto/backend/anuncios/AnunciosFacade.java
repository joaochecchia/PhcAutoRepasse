package repasse.phcauto.backend.anuncios;

import java.util.UUID;
import repasse.phcauto.backend.anuncios.internal.core.PaginaAnuncios;

public interface AnunciosFacade {
    PaginaAnuncios buscar(BuscarAnunciosRequest request);
    AnuncioResponse criar(CriarAnuncioRequest request);
    AnuncioResponse atualizar(UUID anuncioId, AtualizarAnuncioRequest request);
    void excluir(UUID anuncioId);
}
