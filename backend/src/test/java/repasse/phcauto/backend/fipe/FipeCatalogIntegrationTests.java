package repasse.phcauto.backend.fipe;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.JpaOpcoesFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository.*;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http.ParallelumClient;
import repasse.phcauto.backend.fipe.internal.infrastructure.service.*;
@SpringBootTest(properties={"app.fipe.persistence-authorized=false","app.fipe.monthly-cron=-","app.fipe.import-initial-delay=3600000","app.projection.bootstrap=false"})
@EnabledIfEnvironmentVariable(named="FIPE_INTEGRATION_TEST",matches="true")
class FipeCatalogIntegrationTests {
    @Autowired JpaOpcoesFipeGateway opcoes;
    @Autowired FipeCatalogoRepository repository;
    @Autowired FilaSincronizacaoFipe fila;
    @Autowired CatalogoFipeGateway catalogo;
    @Autowired CatalogoLocalFipe cache;
    @Autowired ObjectMapper json;
    @Autowired @org.springframework.beans.factory.annotation.Qualifier("writeDataSource") javax.sql.DataSource writeSource;
    final int referencia=900001;
    ConsultaOpcoesFipe raiz(){return new ConsultaOpcoesFipe(null,null,null,null,"",0,50);}
    ConsultaOpcoesFipe marcas(){return new ConsultaOpcoesFipe("cars",null,null,referencia,"",0,50);}
    @BeforeEach void preparar(){new org.springframework.jdbc.core.JdbcTemplate(writeSource).update("delete from fipe.sincronizacoes where chave like 'teste-lease-%'");opcoes.salvar(raiz(),List.of(new OpcaoFipe(Integer.toString(referencia),"outubro de 2026",LocalDate.of(2026,10,1))));}
    @Test void hierarquiaPaginasUnicidadeEAtualizacaoConcorrente()throws Exception {
        var lista=new ArrayList<OpcaoFipe>();for(int i=1;i<=72;i++)lista.add(new OpcaoFipe(Integer.toString(i),"Marca "+String.format("%03d",i)));
        var pool=Executors.newFixedThreadPool(2);
        try {
            var a=pool.submit(()->opcoes.salvar(marcas(),lista));var b=pool.submit(()->opcoes.salvar(marcas(),lista));
            a.get(20,TimeUnit.SECONDS);b.get(20,TimeUnit.SECONDS);
        }finally{pool.shutdownNow();}
        assertThat(opcoes.listar(marcas(),0,50)).hasSize(50);assertThat(opcoes.listar(marcas(),1,50)).hasSize(22);
        opcoes.salvar(marcas(),List.of(new OpcaoFipe("1","Marca atualizada")));
        assertThat(repository.findByEscopoAndCodigoExterno(EscopoCatalogoFipe.chave(marcas()),"1").orElseThrow().opcao().nome()).isEqualTo("Marca atualizada");
        assertThatThrownBy(()->opcoes.salvar(new ConsultaOpcoesFipe("cars","99999",null,referencia,"",0,50),List.of(new OpcaoFipe("1","Modelo"))))
            .isInstanceOf(FipeException.class);
    }
    @Test void sincronizacaoUsaFilaPersisteEInvalidaCache(){
        var provider=mock(ParallelumClient.class);
        var comando=new SincronizarFipeCommand("motorcycles",null,null,null,referencia,null);
        when(provider.opcoes(comando.opcoes())).thenReturn(List.of(new OpcaoFipe("88","Marca teste")));
        var service=new SincronizacaoFipeService(fila,provider,opcoes,catalogo,cache,json,new SimpleMeterRegistry(),true);
        UUID id=service.agendar(comando);assertThat(service.agendar(comando)).isEqualTo(id);
        service.executar();
        assertThat(service.estado(id).get("status")).isEqualTo("CONCLUIDA");
        assertThat(opcoes.listar(comando.opcoes(),0,50)).contains(new OpcaoFipe("88","Marca teste"));
        verify(provider,times(1)).opcoes(comando.opcoes());
    }
    @Test void politicaNaoAutorizadaNaoAcessaFornecedor(){
        var provider=mock(ParallelumClient.class);
        var service=new SincronizacaoFipeService(fila,provider,opcoes,catalogo,cache,json,new SimpleMeterRegistry(),false);
        assertThatThrownBy(()->service.agendar(new SincronizarFipeCommand(null,null,null,null,null,null))).isInstanceOf(FipeException.class);
        service.executar();verifyNoInteractions(provider);
    }
    @Test void medirLatenciaCatalogoLocalComCache()throws Exception {
        opcoes.salvar(marcas(),List.of(new OpcaoFipe("25","Honda")));
        var modelos=new ConsultaOpcoesFipe("cars","25",null,referencia,"",0,50);
        var anos=new ConsultaOpcoesFipe("cars","25","123",referencia,"",0,50);
        opcoes.salvar(modelos,List.of(new OpcaoFipe("123","Civic EXL 2.0 Flex")));
        opcoes.salvar(anos,List.of(new OpcaoFipe("2022-1","2022 Gasolina")));
        var c=new ConsultaExterna("cars","25","123","2022-1",referencia,LocalDate.of(2026,10,1));
        catalogo.salvarConsulta(new DadosReferencia("014999-9","CARRO","Honda","Civic EXL 2.0 Flex",2022,"Gasolina",c.mesReferencia(),7500000),c.chave());
        Map<String,Runnable> consultas=Map.of("marcas",()->cache.pagina(marcas()),"modelos",()->cache.pagina(modelos),
            "anos",()->cache.pagina(anos),"preco",()->catalogo.buscarConsulta(c.chave(),c.mesReferencia()));
        for(var entry:consultas.entrySet()) {
            for(int j=0;j<10;j++)entry.getValue().run();
            long[] tempos=new long[200];var pool=Executors.newFixedThreadPool(4);
            try {
                var futures=new ArrayList<Future<?>>();
                for(int j=0;j<tempos.length;j++){final int n=j;futures.add(pool.submit(()->{long inicio=System.nanoTime();entry.getValue().run();tempos[n]=System.nanoTime()-inicio;}));}
                for(var future:futures)future.get(10,TimeUnit.SECONDS);
            } finally {pool.shutdownNow();}
            Arrays.sort(tempos);
            System.out.printf(Locale.ROOT,"FIPE_LOCAL_BENCH category=%s n=200 threads=4 p50_ms=%.3f p95_ms=%.3f p99_ms=%.3f%n",
                entry.getKey(),tempos[99]/1e6,tempos[189]/1e6,tempos[197]/1e6);
        }
    }
    @Test void leaseImpedeExecucaoDuplicadaERetomaAposInterrupcao() {
        UUID id=fila.enviar("teste-lease-"+UUID.randomUUID(),"{}");
        var primeira=fila.adquirir().orElseThrow();
        assertThat(primeira.id()).isEqualTo(id);
        assertThat(fila.adquirir()).isEmpty();
        fila.renovar(primeira);
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(writeSource);
        jdbc.update("update fipe.sincronizacoes set lease_ate=now()-interval '1 second' where id=?",id);
        var retomada=fila.adquirir().orElseThrow();
        assertThat(retomada.id()).isEqualTo(id);
        assertThat(retomada.lease()).isNotEqualTo(primeira.lease());
        assertThat(retomada.tentativas()).isEqualTo(2);
        assertThatThrownBy(()->fila.renovar(primeira)).isInstanceOf(org.springframework.dao.InvalidDataAccessApiUsageException.class).hasCauseInstanceOf(IllegalStateException.class);
        fila.concluir(primeira);
        assertThat(fila.estado(id).get("status")).isEqualTo("EXECUTANDO");
        fila.concluir(retomada);
        assertThat(fila.estado(id).get("status")).isEqualTo("CONCLUIDA");
    }
}
