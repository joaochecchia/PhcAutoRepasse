package repasse.phcauto.backend.domain.usecases.identidade;

import repasse.phcauto.backend.domain.model.identidade.UsuarioPf;
import java.util.Optional;
import java.util.UUID;

/** Consulta pelo identificador; retorna vazio quando não encontrado. */
public interface BuscarUsuarioPfPorIdUseCase {

    Optional<UsuarioPf> executar(UUID usuarioId);
}
