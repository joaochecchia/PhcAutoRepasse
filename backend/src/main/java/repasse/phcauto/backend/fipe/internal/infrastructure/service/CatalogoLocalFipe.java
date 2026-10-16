package repasse.phcauto.backend.fipe.internal.infrastructure.service;
import java.time.Duration;
import java.util.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import tools.jackson.databind.ObjectMapper;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.OpcoesFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.JpaOpcoesFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http.ParallelumClient;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository.FilaSincronizacaoFipe;
@Service @Primary
public class CatalogoLocalFipe implements OpcoesFipeGateway {
    private final JpaOpcoesFipeGateway banco;
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final MeterRegistry metrics;
    private final Duration ttl;
    @org.springframework.beans.factory.annotation.Autowired(required=false) private ParallelumClient fornecedor;
    @org.springframework.beans.factory.annotation.Autowired(required=false) private FilaSincronizacaoFipe fila;
    @Value("${app.fipe.persistence-authorized:false}") private boolean persistenciaAutorizada;
    private volatile long redisSuspensoAte;
    @Value("${app.fipe.brands-cache-ttl:PT6H}") private Duration marcasTtl=Duration.ofHours(6);
    @Value("${app.fipe.models-cache-ttl:PT1H}") private Duration modelosTtl=Duration.ofHours(1);
    @Value("${app.fipe.years-cache-ttl:PT1H}") private Duration anosTtl=Duration.ofHours(1);
    private final java.util.concurrent.ConcurrentHashMap<String,java.util.concurrent.CompletableFuture<PaginaOpcoesFipe>> pendentes=new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ExecutorService paralelo=java.util.concurrent.Executors.newFixedThreadPool(4,r->{
        var t=new Thread(r,"fipe-catalogo");t.setDaemon(true);return t;});
    public CatalogoLocalFipe(JpaOpcoesFipeGateway banco,StringRedisTemplate redis,ObjectMapper json,MeterRegistry metrics,
            @Value("${app.fipe.catalog-cache-ttl:PT10M}") Duration ttl) {
        this.banco=banco;this.redis=redis;this.json=json;this.metrics=metrics;this.ttl=ttl;
    }
    public List<OpcaoFipe> opcoes(ConsultaOpcoesFipe c){return banco.listar(c,0,10000);}
    @Override public PaginaOpcoesFipe pagina(ConsultaOpcoesFipe c) {
        var inicio=io.micrometer.core.instrument.Timer.start(metrics);
        try {return carregar(c);}finally{inicio.stop(metrics.timer("phcauto.fipe.catalog.duration"));}
    }
    private PaginaOpcoesFipe carregar(ConsultaOpcoesFipe c) {
        String escopo=EscopoCatalogoFipe.chave(c), geracao="0";
        boolean redisDisponivel=System.nanoTime()>redisSuspensoAte;
        try {String g=redisDisponivel?redis.opsForValue().get("phcauto:fipe:v1:g:"+escopo):null;if(g!=null)geracao=g;}catch(RuntimeException e){redisDisponivel=false;redisSuspensoAte=System.nanoTime()+Duration.ofSeconds(5).toNanos();metrics.counter("phcauto.fipe.cache","resultado","redis_indisponivel").increment();}
        String chave="phcauto:fipe:v2:pagina:"+escopo+":"+geracao+":"+UUID.nameUUIDFromBytes((c.termo()+":"+c.pagina()+":"+c.tamanho()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        try {String v=redisDisponivel?redis.opsForValue().get(chave):null;if(v!=null){metrics.counter("phcauto.fipe.cache","resultado","hit").increment();return json.readValue(v,PaginaOpcoesFipe.class);}}
        catch(RuntimeException ignored) {redisDisponivel=false;redisSuspensoAte=System.nanoTime()+Duration.ofSeconds(5).toNanos();}
        metrics.counter("phcauto.fipe.cache","resultado","miss").increment();
        var futuro=new java.util.concurrent.CompletableFuture<PaginaOpcoesFipe>();
        var existente=pendentes.putIfAbsent(chave,futuro);
        if(existente!=null)return existente.join();
        try {
            boolean dono=false;
            try {dono=redisDisponivel && Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(chave+":lock","1",Duration.ofSeconds(2)));}
            catch(RuntimeException ignored){redisDisponivel=false;redisSuspensoAte=System.nanoTime()+Duration.ofSeconds(5).toNanos();}
            if(!dono && redisDisponivel) {
                // Espera curta e limitada pela reconstrução em outra instância; depois usa PostgreSQL.
                for(int i=0;i<3;i++) {
                    try {
                        Thread.sleep(20);
                        String v=redis.opsForValue().get(chave);
                        if(v!=null){var resultado=json.readValue(v,PaginaOpcoesFipe.class);futuro.complete(resultado);return resultado;}
                    } catch(InterruptedException e){Thread.currentThread().interrupt();break;}
                    catch(RuntimeException e){break;}
                }
            }
            boolean pendente=garantirCarregado(c);
            var lista=banco.listar(c,c.pagina(),c.tamanho());
            boolean proxima=lista.size()==c.tamanho()&&!banco.listar(c,(c.pagina()+1)*c.tamanho(),1).isEmpty();
            var pagina=new PaginaOpcoesFipe(lista,c.pagina(),c.tamanho(),proxima,pendente);
            Duration validade=c.tipo()==null?ttl:c.marcaId()==null?marcasTtl:c.modeloId()==null?modelosTtl:anosTtl;
            if(dono && !lista.isEmpty() && !pendente)try {redis.opsForValue().set(chave,json.writeValueAsString(pagina),validade);}catch(RuntimeException ignored){}
            futuro.complete(pagina);
            return pagina;
        } catch(RuntimeException e){futuro.completeExceptionally(e);throw e;}
        finally {pendentes.remove(chave,futuro);}
    }
    /** Níveis independentes (ex.: os combustíveis de um ano) são garantidos em paralelo para não somar os tempos do fornecedor. */
    @Override public List<CargaOpcoesFipe> carregar(List<ConsultaOpcoesFipe> consultas) {
        if(consultas.size()<=1)return consultas.stream().map(this::cargaCompleta).toList();
        var futuros=consultas.stream().map(c->java.util.concurrent.CompletableFuture.supplyAsync(()->cargaCompleta(c),paralelo)).toList();
        try {return futuros.stream().map(java.util.concurrent.CompletableFuture::join).toList();}
        catch(java.util.concurrent.CompletionException e){throw e.getCause() instanceof RuntimeException r?r:e;}
    }
    private CargaOpcoesFipe cargaCompleta(ConsultaOpcoesFipe c) {
        boolean pendente=garantirCarregado(c);
        return new CargaOpcoesFipe(banco.listar(c,0,10000),pendente);
    }
    /**
     * Sob demanda: o escopo inteiro (todas as marcas, modelos ou anos) vem do fornecedor uma única vez por edição e fica
     * no PostgreSQL; depois disso nenhuma consulta externa é feita para ele. Devolve true quando ficou pendente.
     */
    private boolean garantirCarregado(ConsultaOpcoesFipe c) {
        if(!persistenciaAutorizada || fornecedor==null || banco.escopoCarregado(c))return false;
        try {
            var recebidas=fornecedor.opcoes(new ConsultaOpcoesFipe(c.tipo(),c.marcaId(),c.modeloId(),c.referencia(),"",0,50,c.anoId(),c.anosDaMarca()));
            if(recebidas.size()>10000 || recebidas.stream().map(OpcaoFipe::codigo).distinct().count()!=recebidas.size())
                throw new IllegalStateException("Catálogo FIPE externo inválido");
            for(int i=0;i<recebidas.size();i+=100)
                banco.salvar(c,recebidas.subList(i,Math.min(i+100,recebidas.size())));
            banco.marcarCarregado(c,recebidas.size());
            invalidar(c);
            metrics.counter("phcauto.fipe.catalog.external","resultado","sucesso").increment();
            return false;
        } catch(RuntimeException erro) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("event=fipe_catalog_miss_external_unavailable scope={} reason={}",
                EscopoCatalogoFipe.chave(c),erro instanceof repasse.phcauto.backend.fipe.internal.core.exception.FipeException f?f.motivo():erro.getClass().getSimpleName());
            metrics.counter("phcauto.fipe.catalog.external","resultado","falha").increment();
            agendar(c);
            return true;
        }
    }
    @jakarta.annotation.PreDestroy void encerrar(){paralelo.shutdownNow();}
    /** A fila conclui a carga em segundo plano quando o fornecedor falha durante a consulta do usuário. */
    private void agendar(ConsultaOpcoesFipe c) {
        if(fila==null)return;
        try {
            var comando=SincronizarFipeCommand.opcoes(c);
            fila.enviar(comando.chave(),json.writeValueAsString(comando));
        } catch(RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("event=fipe_catalog_enqueue_failed scope={}",EscopoCatalogoFipe.chave(c));
        }
    }
    public void invalidar(ConsultaOpcoesFipe c) {
        try {redis.opsForValue().set("phcauto:fipe:v1:g:"+EscopoCatalogoFipe.chave(c),UUID.randomUUID().toString(),Duration.ofDays(2));}
        catch(RuntimeException ignored){ /* Cache antigo expira pelo TTL; sincronização permanece persistida. */ }
    }
}
