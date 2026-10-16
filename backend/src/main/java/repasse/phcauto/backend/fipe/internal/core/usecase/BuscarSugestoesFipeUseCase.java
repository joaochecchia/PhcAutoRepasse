package repasse.phcauto.backend.fipe.internal.core.usecase;

import java.util.List;
import repasse.phcauto.backend.fipe.internal.core.domain.BuscarSugestoesFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.SugestaoFipe;

public interface BuscarSugestoesFipeUseCase {
    List<SugestaoFipe> execute(BuscarSugestoesFipe consulta);
}
