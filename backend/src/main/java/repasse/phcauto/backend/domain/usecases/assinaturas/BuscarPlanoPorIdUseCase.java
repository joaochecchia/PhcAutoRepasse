package repasse.phcauto.backend.domain.usecases.assinaturas;

import repasse.phcauto.backend.domain.model.assinaturas.Plano;
import java.util.Optional;
import java.util.UUID;

/** Consulta pelo identificador; retorna vazio quando não encontrado. */
public interface BuscarPlanoPorIdUseCase {

    Optional<Plano> executar(UUID id);
}
