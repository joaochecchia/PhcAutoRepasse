package repasse.phcauto.backend.fipe.internal.core.usecase;
import java.util.*;
import java.time.*;
import repasse.phcauto.backend.fipe.internal.core.domain.DadosReferencia;
import repasse.phcauto.backend.fipe.internal.core.gateway.ImportarLoteFipeGateway;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
public final class ImportarLoteFipe implements ImportarLoteFipeUseCase {
    private final ImportarLoteFipeGateway gateway;
    private final Clock clock;
    public ImportarLoteFipe(ImportarLoteFipeGateway gateway,Clock clock){this.gateway=gateway;this.clock=clock;}
    public void execute(UUID id,String fonte,LocalDate mes,List<DadosReferencia> lote){
        if(id==null || fonte==null || fonte.isBlank() || fonte.length()>120 || mes==null
                || mes.getDayOfMonth()!=1 || mes.isAfter(YearMonth.now(clock).atDay(1))
                || lote==null || lote.size()>100)falhar("Metadados ou tamanho do lote inválidos");
        var chaves=new HashSet<String>();
        for(var d:lote){
            if(!mes.equals(d.mesReferencia()))falhar("Todas as linhas precisam pertencer ao mês da importação");
            if(!chaves.add(d.codigoFipe()+":"+d.anoModelo()+":"+d.combustivel()))falhar("Referência duplicada no lote");
        }
        gateway.salvar(id,fonte,lote);
    }
    private static void falhar(String mensagem){throw new FipeException(FipeException.Motivo.INVALIDA,mensagem);}
}
