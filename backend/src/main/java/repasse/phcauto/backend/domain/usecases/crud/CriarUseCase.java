package repasse.phcauto.backend.domain.usecases.crud;

/**
 * Cria um novo registro e retorna o estado persistido. Não deve sobrescrever um registro existente.
 * Argumentos de referência não podem ser null.
 */
public interface CriarUseCase<T> {

    T criar(T dados);
}
