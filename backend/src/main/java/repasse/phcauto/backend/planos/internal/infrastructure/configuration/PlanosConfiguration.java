package repasse.phcauto.backend.planos.internal.infrastructure.configuration;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repasse.phcauto.backend.planos.internal.core.gateway.PlanoGateway;
import repasse.phcauto.backend.planos.internal.core.usecase.AtualizarPlano;
import repasse.phcauto.backend.planos.internal.core.usecase.AtualizarPlanoUseCase;
import repasse.phcauto.backend.planos.internal.core.usecase.BuscarPlano;
import repasse.phcauto.backend.planos.internal.core.usecase.BuscarPlanoUseCase;
import repasse.phcauto.backend.planos.internal.core.usecase.CriarPlano;
import repasse.phcauto.backend.planos.internal.core.usecase.CriarPlanoUseCase;
import repasse.phcauto.backend.planos.internal.core.usecase.ExcluirPlano;
import repasse.phcauto.backend.planos.internal.core.usecase.ExcluirPlanoUseCase;
import repasse.phcauto.backend.planos.internal.core.usecase.ListarPlanos;
import repasse.phcauto.backend.planos.internal.core.usecase.ListarPlanosUseCase;
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
