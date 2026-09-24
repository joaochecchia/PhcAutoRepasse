package repasse.phcauto.backend.domain.usecases.crud;

import java.util.List;

/**
 * Lista com ordenação estável e visibilidade autorizada. Offset deve ser >= 0 e limite > 0; valores inválidos causam IllegalArgumentException. Retorna lista vazia quando não há resultados.
 * Argumentos de referência não podem ser null.
 */
public interface ListarUseCase<T> {

    List<T> listar(int offset, int limite);
}
