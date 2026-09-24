package repasse.phcauto.backend.domain.usecases.vendas;

import repasse.phcauto.backend.domain.model.vendas.Compra;
import java.util.Optional;
import java.util.UUID;

/** Consulta pelo identificador; retorna vazio quando não encontrado. */
public interface BuscarCompraPorIdUseCase {

    Optional<Compra> executar(UUID id);
}
