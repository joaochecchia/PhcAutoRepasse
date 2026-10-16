package repasse.phcauto.backend.fipe.internal.infrastructure.service;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import tools.jackson.databind.ObjectMapper;
import repasse.phcauto.backend.fipe.internal.core.domain.SincronizarFipeCommand;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http.ParallelumClient;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.JpaOpcoesFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository.FilaSincronizacaoFipe;
@Service
public class SincronizacaoFipeService {
    private final FilaSincronizacaoFipe fila;
    private final ParallelumClient fornecedor;
    private final JpaOpcoesFipeGateway opcoes;
    private final CatalogoFipeGateway catalogo;
    private final CatalogoLocalFipe cache;
    private final ObjectMapper json;
    private final MeterRegistry metrics;
    private final boolean permitido;
    public SincronizacaoFipeService(FilaSincronizacaoFipe fila,ParallelumClient fornecedor,JpaOpcoesFipeGateway opcoes,
            CatalogoFipeGateway catalogo,CatalogoLocalFipe cache,ObjectMapper json,MeterRegistry metrics,
            @Value("${app.fipe.persistence-authorized:false}") boolean permitido) {
        this.fila=fila;this.fornecedor=fornecedor;this.opcoes=opcoes;this.catalogo=catalogo;this.cache=cache;this.json=json;this.metrics=metrics;this.permitido=permitido;
    }
    public UUID agendar(SincronizarFipeCommand c) {
        if(!permitido)throw new FipeException(FipeException.Motivo.CONFLITO,"Sincronização requer política de persistência autorizada na configuração");
        return fila.enviar(c.chave(),json.writeValueAsString(c));
    }
    public Map<String,Object> estado(UUID id){return fila.estado(id);}
    @Scheduled(cron="${app.fipe.sync-refresh-cron:0 15 3 * * *}",zone="America/Sao_Paulo")
    public void atualizarEscoposConhecidos(){if(permitido)fila.reagendarAtuais();}
    @Scheduled(cron="${app.fipe.sync-monthly-cron:0 0 3 1 * *}",zone="America/Sao_Paulo")
    public void verificarNovaEdicao(){if(permitido)agendar(new SincronizarFipeCommand(null,null,null,null,null,null));}
    @Scheduled(fixedDelayString="${app.fipe.sync-delay:5000}",initialDelayString="${app.fipe.sync-initial-delay:30000}")
    public void executar() {
        if(!permitido)return;
        fila.adquirir().ifPresent(t->{
            var tempo=io.micrometer.core.instrument.Timer.start(metrics);
            try {
                var c=json.readValue(t.comando(),SincronizarFipeCommand.class);
                // Nenhuma transação de banco permanece aberta durante o HTTP.
                if(c.anoId()==null){
                    var recebidas=fornecedor.opcoes(c.opcoes());
                    if(recebidas.size()>10000 || recebidas.stream().map(o->o.codigo()).distinct().count()!=recebidas.size())
                        throw new FipeException(FipeException.Motivo.INVALIDA,"Catálogo inválido ou excedeu o limite");
                    for(int i=0;i<recebidas.size();i+=100){fila.renovar(t);opcoes.salvar(c.opcoes(),recebidas.subList(i,Math.min(i+100,recebidas.size())));}
                    opcoes.marcarCarregado(c.opcoes(),recebidas.size());
                    cache.invalidar(c.opcoes());
                    if(c.tipo()==null) {
                        recebidas.stream().filter(o->o.mesReferencia()!=null)
                            .max(Comparator.comparing(repasses -> repasses.mesReferencia()))
                            .ifPresent(edicao->{
                                for(String tipo:List.of("cars","motorcycles","trucks")) {
                                    var marcas=new SincronizarFipeCommand(tipo,null,null,null,Integer.parseInt(edicao.codigo()),null);
                                    fila.enviar(marcas.chave(),json.writeValueAsString(marcas));
                                }
                            });
                    }
                }
                else {var consulta=c.consulta();opcoes.validar(consulta);var dados=fornecedor.consultar(consulta);fila.renovar(t);catalogo.salvarConsulta(dados,consulta.chave());}
                fila.concluir(t);metrics.counter("phcauto.fipe.sync","resultado","sucesso").increment();
            } catch(RuntimeException e) {
                boolean transitoria=e instanceof FipeException f&&(f.motivo()==FipeException.Motivo.INDISPONIVEL||f.motivo()==FipeException.Motivo.LIMITE);
                fila.falhar(t,e instanceof FipeException f?f.motivo().name():"FALHA_INTERNA",transitoria);
                LoggerFactory.getLogger(getClass()).warn("event=fipe_sync_failed job={} attempt={} transient={}",t.id(),t.tentativas(),transitoria);
                metrics.counter("phcauto.fipe.sync","resultado","falha").increment();
            } finally {tempo.stop(metrics.timer("phcauto.fipe.sync.duration"));}
        });
    }
}
