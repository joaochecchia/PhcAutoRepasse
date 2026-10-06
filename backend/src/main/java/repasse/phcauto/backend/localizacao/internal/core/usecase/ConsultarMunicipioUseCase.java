package repasse.phcauto.backend.localizacao.internal.core.usecase;

import repasse.phcauto.backend.localizacao.internal.core.domain.Municipio;

public interface ConsultarMunicipioUseCase {
    Municipio buscarPorCidadeEUf(String cidade, String uf);
    Municipio buscarPorCodigoIbge(int codigoIbge);
}
