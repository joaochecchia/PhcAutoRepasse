package repasse.phcauto.backend.anuncios.internal.core.usecase;

import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioCriadoResultado;
import repasse.phcauto.backend.anuncios.internal.core.domain.CriarAnuncioCommand;

public interface CriarAnuncioUseCase {
    AnuncioCriadoResultado execute(CriarAnuncioCommand command);
}
