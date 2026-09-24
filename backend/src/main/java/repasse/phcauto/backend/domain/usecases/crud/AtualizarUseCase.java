package repasse.phcauto.backend.domain.usecases.crud;

/**
 * Atualiza um registro existente sem alterar sua identidade. Não cria registros ausentes; nesse caso lança NoSuchElementException. Se os dados contiverem uma identidade diferente, lança IllegalArgumentException.
 * Argumentos de referência não podem ser null.
 */
public interface AtualizarUseCase<T, ID> {

    T atualizar(ID id, T dados);
}
