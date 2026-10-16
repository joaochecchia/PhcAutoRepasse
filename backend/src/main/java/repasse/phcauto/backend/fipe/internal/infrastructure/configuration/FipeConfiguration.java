package repasse.phcauto.backend.fipe.internal.infrastructure.configuration;
import java.time.Clock;
import org.springframework.context.annotation.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.*;
import repasse.phcauto.backend.fipe.internal.core.usecase.*;
@Configuration(proxyBeanMethods=false)
public class FipeConfiguration {
    @Bean ConsultarOpcoesFipeUseCase consultarOpcoesFipe(OpcoesFipeGateway gateway){return new ConsultarOpcoesFipe(gateway);}
    @Bean BuscarEdicaoAtivaFipeUseCase buscarEdicaoAtivaFipe(EdicaoAtivaFipeGateway gateway){return new BuscarEdicaoAtivaFipeUseCase(gateway);}
    @Bean BuscarSugestoesFipeUseCase buscarSugestoesFipe(OpcoesFipeGateway gateway){return new BuscarSugestoesFipe(gateway);}
    @Bean ResolverReferenciaFipeUseCase resolverReferenciaFipe(CatalogoFipeGateway catalogo,ReferenciaSobDemandaFipeGateway sobDemanda){return new ResolverReferenciaFipe(catalogo,sobDemanda);}
    @Bean PesquisarFipeUseCase pesquisarFipe(CatalogoFipeGateway catalogo){return new PesquisarFipe(catalogo);}
    @Bean ImportarLoteFipeUseCase importarLoteFipe(ImportarLoteFipeGateway gateway,Clock clock){return new ImportarLoteFipe(gateway,clock);}
}
