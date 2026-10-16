package repasse.phcauto.backend.fipe.internal.core.usecase;
import java.util.*;
import java.util.stream.*;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.OpcoesFipeGateway;
/**
 * Modelos da marca no ano escolhido, somando os combustíveis daquele ano. Cada opção carrega o ano/combustível
 * (anoId) usado depois na consulta de preço; o combustível só aparece no nome quando o mesmo modelo existe em mais de um.
 */
public final class ConsultarModelosAnoFipe implements ConsultarModelosAnoFipeUseCase {
    private final OpcoesFipeGateway gateway;
    public ConsultarModelosAnoFipe(OpcoesFipeGateway gateway){this.gateway=gateway;}
    public PaginaOpcoesFipe execute(ConsultaModelosAnoFipe c){
        var anos=gateway.carregar(List.of(c.anosDaMarca())).get(0);
        String ano=Integer.toString(c.ano());
        var doAno=anos.opcoes().stream().filter(o->AnosFipe.ano(o).equals(ano)).toList();
        var cargas=gateway.carregar(doAno.stream().map(o->c.modelos(o.codigo())).toList());
        boolean pendente=anos.pendente() || cargas.stream().anyMatch(CargaOpcoesFipe::pendente);
        var modelos=new ArrayList<OpcaoFipe>();
        var combustiveis=new HashMap<OpcaoFipe,String>();
        for(int i=0;i<doAno.size();i++)
            for(var modelo:cargas.get(i).opcoes()) {
                var opcao=new OpcaoFipe(modelo.codigo(),modelo.nome(),null,doAno.get(i).codigo());
                modelos.add(opcao);
                combustiveis.put(opcao,AnosFipe.combustivel(doAno.get(i)));
            }
        var repetidos=modelos.stream().collect(Collectors.groupingBy(OpcaoFipe::nome,Collectors.counting()));
        var nomeados=modelos.stream()
            .map(o->repetidos.get(o.nome())>1 && !combustiveis.get(o).isBlank()
                ?new OpcaoFipe(o.codigo(),o.nome()+" — "+combustiveis.get(o),null,o.anoId()):o)
            .sorted(Comparator.comparing(OpcaoFipe::nome,String.CASE_INSENSITIVE_ORDER).thenComparing(OpcaoFipe::anoId))
            .toList();
        return AnosFipe.paginar(nomeados,c.termo(),c.pagina(),c.tamanho(),pendente);
    }
}
