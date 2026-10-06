package repasse.phcauto.backend.localizacao.internal.infrastructure.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import repasse.phcauto.backend.localizacao.LocalizacaoFacade;
import repasse.phcauto.backend.localizacao.MunicipioLocalizacao;
import repasse.phcauto.backend.localizacao.internal.core.domain.Municipio;
import repasse.phcauto.backend.localizacao.internal.core.usecase.ConsultarMunicipioUseCase;

@Service
class DefaultLocalizacaoFacade implements LocalizacaoFacade {
    private final ConsultarMunicipioUseCase consultarMunicipio;

    DefaultLocalizacaoFacade(ConsultarMunicipioUseCase consultarMunicipio) {
        this.consultarMunicipio = consultarMunicipio;
    }

    @Override
    @Cacheable(cacheNames = "municipiosPorNome", key = "(#uf + ':' + #cidade).toUpperCase()")
    public MunicipioLocalizacao buscarMunicipio(String cidade, String uf) {
        return mapear(consultarMunicipio.buscarPorCidadeEUf(cidade, uf));
    }

    @Override
    @Cacheable(cacheNames = "municipiosPorCodigo", key = "#codigoIbge")
    public MunicipioLocalizacao buscarMunicipio(int codigoIbge) {
        return mapear(consultarMunicipio.buscarPorCodigoIbge(codigoIbge));
    }

    private static MunicipioLocalizacao mapear(Municipio municipio) {
        return new MunicipioLocalizacao(municipio.codigoIbge(), municipio.nome(), municipio.uf(),
                municipio.latitude(), municipio.longitude());
    }
}
