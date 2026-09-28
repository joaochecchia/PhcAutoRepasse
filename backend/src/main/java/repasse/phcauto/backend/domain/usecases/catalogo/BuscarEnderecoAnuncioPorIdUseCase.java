package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.Optional;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.EnderecoAnuncio;

public interface BuscarEnderecoAnuncioPorIdUseCase {
    Optional<EnderecoAnuncio> executar(UUID enderecoId);
}
