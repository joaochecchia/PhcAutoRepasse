package repasse.phcauto.backend.anuncios.internal.infrastructure.service;

import java.util.UUID;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.PaginaAnunciosResponse;

import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.AtualizarAnuncioRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.BuscarAnunciosRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.CriarAnuncioRequest;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.response.AnuncioResponse;
public interface AnunciosFacade {
    PaginaAnunciosResponse buscar(BuscarAnunciosRequest request);
    AnuncioResponse criar(CriarAnuncioRequest request);
    AnuncioResponse atualizar(UUID anuncioId, AtualizarAnuncioRequest request);
    void excluir(UUID anuncioId);
}
