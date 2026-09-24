package repasse.phcauto.backend.domain.usecases.crud;

import java.util.Optional;

/**
 * Busca pela chave completa. Retorna Optional.empty() quando o registro não existe.
 * Argumentos de referência não podem ser null.
 */
public interface BuscarPorIdUseCase<T, ID> {

    Optional<T> buscarPorId(ID id);
}
