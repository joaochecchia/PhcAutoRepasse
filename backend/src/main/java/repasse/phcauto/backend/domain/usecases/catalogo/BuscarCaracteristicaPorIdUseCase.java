package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Caracteristica;
import java.util.Optional;
import java.util.UUID;

/** Consulta pelo identificador; retorna vazio quando não encontrado. */
public interface BuscarCaracteristicaPorIdUseCase {

    Optional<Caracteristica> executar(UUID id);
}
