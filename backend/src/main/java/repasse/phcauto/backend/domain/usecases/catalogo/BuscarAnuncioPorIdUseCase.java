package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Anuncio;
import java.util.Optional;
import java.util.UUID;

/** Consulta pelo identificador; retorna vazio quando não encontrado. */
public interface BuscarAnuncioPorIdUseCase {

    Optional<Anuncio> executar(UUID id);
}
