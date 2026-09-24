package repasse.phcauto.backend.domain.usecases.identidade;

import repasse.phcauto.backend.domain.model.identidade.UsuarioPj;
import java.util.Optional;
import java.util.UUID;

/** Consulta pelo identificador; retorna vazio quando não encontrado. */
public interface BuscarUsuarioPjPorIdUseCase {

    Optional<UsuarioPj> executar(UUID usuarioId);
}
