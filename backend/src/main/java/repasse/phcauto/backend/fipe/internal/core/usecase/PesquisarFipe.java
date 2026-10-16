package repasse.phcauto.backend.fipe.internal.core.usecase;
import java.util.List;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.PesquisaFipe;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
public final class PesquisarFipe implements PesquisarFipeUseCase {
    private final CatalogoFipeGateway gateway;
    public PesquisarFipe(CatalogoFipeGateway gateway){this.gateway=gateway;}
    public List<ReferenciaFipe> execute(PesquisaFipe p){return gateway.pesquisar(p.tipo(),p.marca(),p.termo(),p.ano(),p.mes(),p.pagina(),p.tamanho());}
}
