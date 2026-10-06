package repasse.phcauto.backend.localizacao.internal.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repasse.phcauto.backend.localizacao.internal.core.gateway.ConsultarMunicipioGateway;
import repasse.phcauto.backend.localizacao.internal.core.usecase.ConsultarMunicipio;
import repasse.phcauto.backend.localizacao.internal.core.usecase.ConsultarMunicipioUseCase;

@Configuration(proxyBeanMethods = false)
class LocalizacaoConfig {
    @Bean
    ConsultarMunicipioUseCase consultarMunicipioUseCase(ConsultarMunicipioGateway gateway) {
        return new ConsultarMunicipio(gateway);
    }
}
