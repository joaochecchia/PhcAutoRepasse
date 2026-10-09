package repasse.phcauto.backend.anuncios.internal.core.usecase;

import java.util.List;
import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioResumo;

public interface BuscarAnunciosPaginaInicialUseCase {
    List<AnuncioResumo> execute(int limite);
}
