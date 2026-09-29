package repasse.phcauto.backend.planos.internal.infrastructure;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repasse.phcauto.backend.planos.internal.core.*;

@Configuration(proxyBeanMethods = false)
class PlanosConfiguration {
    @Bean CriarPlanoUseCase criarPlanoUseCase(PlanoGateway gateway, Clock applicationClock) {
        return new CriarPlano(gateway, applicationClock);
    }
    @Bean BuscarPlanoUseCase buscarPlanoUseCase(PlanoGateway gateway) { return new BuscarPlano(gateway); }
    @Bean ListarPlanosUseCase listarPlanosUseCase(PlanoGateway gateway) { return new ListarPlanos(gateway); }
    @Bean AtualizarPlanoUseCase atualizarPlanoUseCase(PlanoGateway gateway) { return new AtualizarPlano(gateway); }
    @Bean ExcluirPlanoUseCase excluirPlanoUseCase(PlanoGateway gateway) { return new ExcluirPlano(gateway); }
}
