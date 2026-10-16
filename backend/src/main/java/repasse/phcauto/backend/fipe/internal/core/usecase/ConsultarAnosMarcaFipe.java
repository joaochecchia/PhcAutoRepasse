package repasse.phcauto.backend.fipe.internal.core.usecase;
import java.util.*;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.OpcoesFipeGateway;
/** Anos-modelo disponíveis para a marca, sem repetir o ano para cada combustível; zero km primeiro. */
public final class ConsultarAnosMarcaFipe implements ConsultarAnosMarcaFipeUseCase {
    private final OpcoesFipeGateway gateway;
    public ConsultarAnosMarcaFipe(OpcoesFipeGateway gateway){this.gateway=gateway;}
    public PaginaOpcoesFipe execute(ConsultaOpcoesFipe c){
        var carga=gateway.carregar(List.of(ConsultaOpcoesFipe.anosDaMarca(c.tipo(),c.marcaId(),c.referencia()))).get(0);
        var anos=carga.opcoes().stream().map(AnosFipe::ano).distinct()
            .sorted(Comparator.comparingInt((String ano)->Integer.parseInt(ano)).reversed())
            .map(ano->new OpcaoFipe(ano,AnosFipe.nomeAno(ano))).toList();
        return AnosFipe.paginar(anos,c.termo(),c.pagina(),c.tamanho(),carga.pendente());
    }
}
