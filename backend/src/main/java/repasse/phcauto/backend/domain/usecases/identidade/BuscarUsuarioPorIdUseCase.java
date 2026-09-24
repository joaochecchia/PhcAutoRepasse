package repasse.phcauto.backend.domain.usecases.identidade;

import repasse.phcauto.backend.domain.model.identidade.Usuario;
import java.util.Optional;
import java.util.UUID;

/** Consulta pelo identificador; retorna vazio quando não encontrado. */
public interface BuscarUsuarioPorIdUseCase {

    Optional<Usuario> executar(UUID id);
}
