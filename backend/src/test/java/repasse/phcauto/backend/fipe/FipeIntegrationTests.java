package repasse.phcauto.backend.fipe;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tools.jackson.databind.ObjectMapper;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.service.ImportacaoFipeService;
import repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request.*;
import repasse.phcauto.backend.anuncios.internal.infrastructure.service.AnunciosFacade;
@SpringBootTest(properties={"app.fipe.import-initial-delay=3600000","app.fipe.import-delay=3600000","app.fipe.monthly-cron=-",
        "app.fipe.import-directory=/tmp/phc-fipe-integration","app.projection.bootstrap=false"})
@EnabledIfEnvironmentVariable(named="FIPE_INTEGRATION_TEST",matches="true")
class FipeIntegrationTests {
    @Autowired CatalogoFipeGateway catalogo;
    @Autowired ImportacaoFipeService importacao;
    @Autowired AnunciosFacade anuncios;
    @Autowired ObjectMapper json;
    @Autowired WebApplicationContext web;
    @Autowired @Qualifier("writeDataSource")DataSource writeSource;
    @Autowired @Qualifier("projectionDataSource")DataSource readSource;
    @Autowired @Qualifier("writeTransactionManager")PlatformTransactionManager manager;
    final LocalDate mes=LocalDate.of(2026,10,1);
    @BeforeEach void concluirPendentes(){for(int i=0;i<10;i++)importacao.processar();}
    DadosReferencia dados(){return new DadosReferencia("014001-4","CARRO","Honda","Civic EXL Flex",2022,"Gasolina",mes,7500000);}
    String linha(String codigo){return """
        {"codigoFipe":"%s","tipo":"CARRO","marca":"Honda","modelo":"Civic EXL Flex","anoModelo":2022,"combustivel":"Gasolina","mesReferencia":"2026-10-01","valorCentavos":7500000}
        """.formatted(codigo);}
    @Test void importacaoAtomicaVisibilidadeIdempotenciaERetomada()throws Exception{
        String fonte="TESTE-"+UUID.randomUUID();
        String conteudo=linha("014002-2");
        var estado=importacao.enviar(new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8)),fonte,mes);
        assertThat(importacao.consultar(estado.id()).status()).isEqualTo("PENDENTE");
        var igual=importacao.enviar(new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8)),fonte,mes);
        assertThat(igual.id()).isEqualTo(estado.id());
        importacao.processar();
        assertThat(importacao.consultar(estado.id()).status()).isEqualTo("CONCLUIDA");
        assertThat(importacao.consultar(estado.id()).registros()).isEqualTo(1);
        var write=new JdbcTemplate(writeSource);var read=new JdbcTemplate(readSource);
        UUID id=write.queryForObject("select id from fipe.referencias where importacao_id=?",UUID.class,estado.id());
        assertThat(catalogo.buscar(id)).isPresent();
        await().atMost(Duration.ofSeconds(15)).untilAsserted(()->assertThat(read.queryForObject("select count(*) from fipe.referencias where id=?",Integer.class,id)).isEqualTo(1));
        // Um lote já confirmado permanece invisível se o próximo lote falhar.
        StringBuilder arquivo=new StringBuilder();for(int i=100000;i<100100;i++)arquivo.append(linha(i+"-1"));
        arquivo.append("{invalido}\n");
        var falha=importacao.enviar(new ByteArrayInputStream(arquivo.toString().getBytes(StandardCharsets.UTF_8)),fonte+"-falha",mes);
        importacao.processar();assertThat(importacao.consultar(falha.id()).registros()).isEqualTo(100);
        UUID pendente=write.queryForObject("select id from fipe.referencias where importacao_id=? limit 1",UUID.class,falha.id());
        assertThat(catalogo.buscar(pendente)).isEmpty();
        importacao.processar();assertThat(importacao.consultar(falha.id()).status()).isEqualTo("FALHOU");
        assertThat(catalogo.buscar(pendente)).isEmpty();
        importacao.retomar(falha.id());importacao.processar();assertThat(importacao.consultar(falha.id()).status()).isEqualTo("FALHOU");
        assertThat(write.queryForObject("select count(*) from fipe.referencias where importacao_id=?",Integer.class,falha.id())).isEqualTo(100);
    }
    @Test void concorrenciaPersisteUmaReferenciaERollbackNaoPublica()throws Exception{
        var pool=Executors.newFixedThreadPool(4);
        try{
            var tarefas=new ArrayList<Future<ReferenciaFipe>>();
            for(int i=0;i<4;i++)tarefas.add(pool.submit(()->catalogo.salvarConsulta(dados(),"teste-concorrencia")));
            var ids=new HashSet<UUID>();for(var tarefa:tarefas)ids.add(tarefa.get(20,TimeUnit.SECONDS).id());
            assertThat(ids).hasSize(1);
        }finally{pool.shutdownNow();}
        var tx=new TransactionTemplate(manager);UUID[] id={null};
        tx.executeWithoutResult(s->{id[0]=catalogo.salvarConsulta(new DadosReferencia("014099-1","CARRO","Honda","Civic",2022,"Gasolina",mes,1),"rollback").id();s.setRollbackOnly();});
        assertThat(catalogo.buscar(id[0])).isEmpty();
        assertThat(new JdbcTemplate(readSource).queryForObject("select count(*) from fipe.referencias where id=?",Integer.class,id[0])).isZero();
    }
    @Test void snapshotNoVeiculoCardsPatchERollback()throws Exception{
        var r=catalogo.salvarConsulta(dados(),"teste-card");
        var write=new JdbcTemplate(writeSource);var read=new JdbcTemplate(readSource);UUID usuario=UUID.randomUUID();
        // Fixture isolada; não são mutações de negócio da aplicação.
        String sql="insert into identidade.usuarios(id,nome,email,tipo_pessoa,papel,ativo,criado_em,atualizado_em) values (?,?,?,'PF','CLIENTE',true,now(),now())";
        write.update(sql,usuario,"FIPE Teste",usuario+"@teste.local");read.update(sql,usuario,"FIPE Teste",usuario+"@teste.local");
        String body="""
          {"anuncianteId":"%s","tipoVeiculo":"CARRO","fabricante":"Honda","modelo":"Civic","versao":"Civic EXL Flex",
          "anoFabricacao":2022,"anoModelo":2022,"condicao":"USADO","historicoLeilao":false,"historicoSinistro":false,
          "titulo":"Teste FIPE","tipoPreco":"FIXO","precoCentavos":7290000,"publicarAgora":true,"fipeReferenciaId":"%s",
          "endereco":{"cep":"70000000","cidade":"Brasília","bairro":"Centro","rua":"Rua teste","numero":"1","uf":"DF"},
          "carro":{"quilometragem":1000,"carroceria":"Sedã","cambio":"Automático","combustivel":"FLEX","motorizacao":"2.0",
          "placa":"ABC1D23","exibirPlacaCompleta":false,"unicoDono":true,"ipvaPago":true}}
          """.formatted(usuario,r.id());
        var criado=anuncios.criar(json.readValue(body,CriarAnuncioRequest.class));
        assertThat(criado.fipe().valorCentavos()).isEqualTo(7500000);
        assertThat(write.queryForObject("select fipe_codigo from catalogo.veiculos where id=?",String.class,criado.veiculoId())).isEqualTo(r.codigoFipe());
        await().atMost(Duration.ofSeconds(15)).untilAsserted(()->assertThat(read.queryForList("select fipe_valor_centavos from catalogo.veiculos where id=?",Long.class,criado.veiculoId())).containsExactly(7500000L));
        // A projeção é eventual por linha: veículo projetado não implica anúncio/endereço já disponíveis.
        await().atMost(Duration.ofSeconds(15)).untilAsserted(()->assertThat(anuncios.buscar(null,null).anuncios())
            .anySatisfy(card->{assertThat(card.anuncioId()).isEqualTo(criado.id());assertThat(card.fipe().diferencaPercentual()).isEqualByComparingTo("-2.80");}));
        var alterado=anuncios.atualizar(criado.id(),json.readValue("{\"precoCentavos\":8000000}",AtualizarAnuncioRequest.class));
        assertThat(alterado.fipe().valorCentavos()).isEqualTo(7500000);
        var tx=new TransactionTemplate(manager);
        tx.executeWithoutResult(s->{anuncios.atualizar(criado.id(),json.readValue("{\"removerFipe\":true}",AtualizarAnuncioRequest.class));s.setRollbackOnly();});
        assertThat(write.queryForObject("select fipe_codigo from catalogo.veiculos where id=?",String.class,criado.veiculoId())).isEqualTo(r.codigoFipe());
        var removido=anuncios.atualizar(criado.id(),json.readValue("{\"versao\":\"Outra versão\"}",AtualizarAnuncioRequest.class));
        assertThat(removido.fipe()).isNull();
        assertThat(write.queryForObject("select fipe_codigo from catalogo.veiculos where id=?",String.class,criado.veiculoId())).isNull();
        anuncios.excluir(criado.id());assertThat(catalogo.buscar(r.id())).isPresent();
    }
    @Test void endpointsExigemAutenticacaoEDonoParaImportacao()throws Exception{
        var mvc=MockMvcBuilders.webAppContextSetup(web).apply(springSecurity()).build();
        mvc.perform(get("/api/v1/fipe/referencias")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/fipe/referencias").with(jwt())).andExpect(status().isOk());
        mvc.perform(get("/api/v1/fipe/referencias?tamanho=500").with(jwt())).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/fipe/importacoes?fonte=TESTE&mes=2026-10-01").contentType("application/x-ndjson")
            .content(linha("014001-4")).with(jwt()).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/fipe/importacoes?fonte=TESTE-HTTP&mes=2026-10-01").contentType("application/x-ndjson")
            .content(linha("014001-4")).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DONO"))).with(csrf())).andExpect(status().isAccepted());
    }
}
