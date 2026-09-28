package repasse.phcauto.backend.anuncios.internal.infrastructure;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repasse.phcauto.backend.anuncios.internal.core.*;

@Configuration(proxyBeanMethods = false)
class AnunciosConfiguration {
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
