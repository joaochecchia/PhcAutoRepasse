package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Caminhonete;
import java.util.Optional;
import java.util.UUID;

/** Contrato dos detalhes do veículo; a implementação deve respeitar o tipo correspondente. */
public interface BuscarCaminhonetePorVeiculoIdUseCase {

    Optional<Caminhonete> executar(UUID veiculoId);
}
