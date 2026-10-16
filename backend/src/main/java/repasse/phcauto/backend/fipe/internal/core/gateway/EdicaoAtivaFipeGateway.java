package repasse.phcauto.backend.fipe.internal.core.gateway;

import java.util.Optional;
import repasse.phcauto.backend.fipe.internal.core.domain.OpcaoFipe;

public interface EdicaoAtivaFipeGateway {
    Optional<OpcaoFipe> buscar(String tipo);
}
