package repasse.phcauto.backend.anuncios.internal.core.gateway;

import java.util.List;
import repasse.phcauto.backend.anuncios.internal.core.domain.AnuncioResumo;

public interface BuscarAnunciosPaginaInicialGateway {
    List<AnuncioResumo> buscar(int limite);
}
