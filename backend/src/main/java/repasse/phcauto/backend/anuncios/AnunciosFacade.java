package repasse.phcauto.backend.anuncios;

import java.util.UUID;

public interface AnunciosFacade {
    AnuncioResponse criar(CriarAnuncioRequest request);
    AnuncioResponse atualizar(UUID anuncioId, AtualizarAnuncioRequest request);
    void excluir(UUID anuncioId);
}
