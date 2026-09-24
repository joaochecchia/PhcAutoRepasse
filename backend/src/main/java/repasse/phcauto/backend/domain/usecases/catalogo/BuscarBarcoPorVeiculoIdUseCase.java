package repasse.phcauto.backend.domain.usecases.catalogo;

import repasse.phcauto.backend.domain.model.catalogo.Barco;
import java.util.Optional;
import java.util.UUID;

/** Contrato dos detalhes do veículo; a implementação deve respeitar o tipo correspondente. */
public interface BuscarBarcoPorVeiculoIdUseCase {

    Optional<Barco> executar(UUID veiculoId);
}
