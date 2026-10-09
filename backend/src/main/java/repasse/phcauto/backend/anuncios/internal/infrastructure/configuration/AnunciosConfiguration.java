package repasse.phcauto.backend.anuncios.internal.infrastructure.configuration;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repasse.phcauto.backend.anuncios.internal.core.gateway.AtualizarAnuncioGateway;
import repasse.phcauto.backend.anuncios.internal.core.gateway.BuscarAnunciosGateway;
import repasse.phcauto.backend.anuncios.internal.core.gateway.BuscarAnunciosPaginaInicialGateway;
import repasse.phcauto.backend.anuncios.internal.core.gateway.CriarAnuncioGateway;
import repasse.phcauto.backend.anuncios.internal.core.gateway.ExcluirAnuncioGateway;
import repasse.phcauto.backend.anuncios.internal.core.gateway.PublicarAlteracaoAnuncioGateway;
import repasse.phcauto.backend.anuncios.internal.core.gateway.PublicarAnuncioCriadoGateway;
import repasse.phcauto.backend.anuncios.internal.core.usecase.AtualizarAnuncio;
import repasse.phcauto.backend.anuncios.internal.core.usecase.AtualizarAnuncioUseCase;
import repasse.phcauto.backend.anuncios.internal.core.usecase.BuscarAnuncios;
import repasse.phcauto.backend.anuncios.internal.core.usecase.BuscarAnunciosUseCase;
import repasse.phcauto.backend.anuncios.internal.core.usecase.BuscarAnunciosPaginaInicial;
import repasse.phcauto.backend.anuncios.internal.core.usecase.BuscarAnunciosPaginaInicialUseCase;
import repasse.phcauto.backend.anuncios.internal.core.usecase.CriarAnuncio;
import repasse.phcauto.backend.anuncios.internal.core.usecase.CriarAnuncioUseCase;
import repasse.phcauto.backend.anuncios.internal.core.usecase.ExcluirAnuncio;
import repasse.phcauto.backend.anuncios.internal.core.usecase.ExcluirAnuncioUseCase;
@Configuration(proxyBeanMethods = false)
class AnunciosConfiguration {
    @Bean BuscarAnunciosUseCase buscarAnunciosUseCase(BuscarAnunciosGateway gateway) {
        return new BuscarAnuncios(gateway);
    }
    @Bean BuscarAnunciosPaginaInicialUseCase buscarAnunciosPaginaInicialUseCase(
            BuscarAnunciosPaginaInicialGateway gateway) {
        return new BuscarAnunciosPaginaInicial(gateway);
    }
    @Bean CriarAnuncioUseCase criarAnuncioUseCase(CriarAnuncioGateway gateway,
            PublicarAnuncioCriadoGateway eventos, Clock applicationClock) {
        return new CriarAnuncio(gateway, eventos, applicationClock);
    }
    @Bean AtualizarAnuncioUseCase atualizarAnuncioUseCase(AtualizarAnuncioGateway gateway,
            PublicarAlteracaoAnuncioGateway eventos, Clock applicationClock) {
        return new AtualizarAnuncio(gateway, eventos, applicationClock);
    }
    @Bean ExcluirAnuncioUseCase excluirAnuncioUseCase(ExcluirAnuncioGateway gateway,
            PublicarAlteracaoAnuncioGateway eventos, Clock applicationClock) {
        return new ExcluirAnuncio(gateway, eventos, applicationClock);
    }
}
