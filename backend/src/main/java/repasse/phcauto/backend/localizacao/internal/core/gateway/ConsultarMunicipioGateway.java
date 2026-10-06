package repasse.phcauto.backend.localizacao.internal.core.gateway;

import java.util.Optional;
import repasse.phcauto.backend.localizacao.internal.core.domain.Municipio;

public interface ConsultarMunicipioGateway {
    Optional<Municipio> buscarPorCidadeEUf(String cidade, String uf);
    Optional<Municipio> buscarPorCodigoIbge(int codigoIbge);
}
