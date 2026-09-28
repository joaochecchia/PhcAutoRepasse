package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.EnderecoAnuncio;

/** Persiste a localização vinculada a um anúncio dentro da transação do agregado. */
public interface SalvarEnderecoAnuncioUseCase {
    EnderecoAnuncio executar(EnderecoAnuncio endereco);
}
