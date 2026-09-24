package repasse.phcauto.backend.domain.usecases.catalogo;

import java.util.List;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.VeiculoCaracteristica;

/** Retorna as associações de características do veículo; lista vazia quando não houver associações. */
public interface ListarCaracteristicasDoVeiculoUseCase {

    List<VeiculoCaracteristica> executar(UUID veiculoId);
}
