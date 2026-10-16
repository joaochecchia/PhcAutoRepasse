package repasse.phcauto.backend.fipe.internal.core.gateway;

import java.util.Optional;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.ConsultaExterna;

public interface ReferenciaSobDemandaFipeGateway {
    Optional<ReferenciaFipe> buscar(ConsultaExterna consulta);
}
