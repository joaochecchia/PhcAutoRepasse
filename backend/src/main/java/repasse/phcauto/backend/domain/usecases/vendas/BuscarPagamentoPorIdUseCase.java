package repasse.phcauto.backend.domain.usecases.vendas;

import repasse.phcauto.backend.domain.model.vendas.Pagamento;
import java.util.Optional;
import java.util.UUID;

/** Consulta pelo identificador; retorna vazio quando não encontrado. */
public interface BuscarPagamentoPorIdUseCase {

    Optional<Pagamento> executar(UUID id);
}
