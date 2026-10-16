package repasse.phcauto.backend.fipe.internal.infrastructure.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.*;
import tools.jackson.databind.json.JsonMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http.ParallelumClient;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.JpaOpcoesFipeGateway;
import repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence.repository.FilaSincronizacaoFipe;
class CatalogoLocalFipeTests {
    final JpaOpcoesFipeGateway banco=mock(JpaOpcoesFipeGateway.class);
    final StringRedisTemplate redis=mock(StringRedisTemplate.class);
    final ValueOperations<String,String> valores=mock(ValueOperations.class);
    final ConsultaOpcoesFipe consulta=new ConsultaOpcoesFipe("cars",null,null,338,"",0,50);
    final List<OpcaoFipe> marcas=List.of(new OpcaoFipe("25","Honda"));
    CatalogoLocalFipe service;
    @BeforeEach void preparar(){
        when(redis.opsForValue()).thenReturn(valores);
        when(valores.setIfAbsent(anyString(),anyString(),any(Duration.class))).thenReturn(true);
        when(banco.listar(consulta,0,50)).thenReturn(marcas);
        service=new CatalogoLocalFipe(banco,redis,JsonMapper.builder().findAndAddModules().build(),new SimpleMeterRegistry(),Duration.ofMinutes(10));
    }
    @Test void missConsultaBancoEArmazenaPagina(){
        assertThat(service.pagina(consulta).opcoes()).isEqualTo(marcas);
        verify(valores).set(contains(":pagina:"),anyString(),eq(Duration.ofHours(6)));
    }
    @Test void hitNaoConsultaBanco(){
        when(valores.get(contains(":pagina:"))).thenReturn("{\"opcoes\":[{\"codigo\":\"25\",\"nome\":\"Honda\",\"mesReferencia\":null}],\"pagina\":0,\"tamanho\":50,\"temProximaPagina\":false,\"catalogoPendente\":false}");
        assertThat(service.pagina(consulta).opcoes()).isEqualTo(marcas);verifyNoInteractions(banco);
    }
    @Test void redisIndisponivelUsaPostgres(){
        when(valores.get(anyString())).thenThrow(new IllegalStateException("offline"));
        when(valores.setIfAbsent(anyString(),anyString(),any(Duration.class))).thenThrow(new IllegalStateException("offline"));
        assertThat(service.pagina(consulta).opcoes()).isEqualTo(marcas);
    }
    @Test void vazioNaoEntraNoCache(){
        when(banco.listar(consulta,0,50)).thenReturn(List.of());
        assertThat(service.pagina(consulta).opcoes()).isEmpty();verify(valores,never()).set(anyString(),anyString(),any(Duration.class));
    }
    @Test void escopoParcialSemMarcaBuscaFornecedorEMarcaCarregado(){
        var fornecedor=mock(ParallelumClient.class);
        var completas=List.of(new OpcaoFipe("25","Honda"),new OpcaoFipe("31","Kia Motors"));
        when(fornecedor.opcoes(any())).thenReturn(completas);
        when(banco.listar(consulta,0,50)).thenReturn(completas);
        ReflectionTestUtils.setField(service,"fornecedor",fornecedor);
        ReflectionTestUtils.setField(service,"persistenciaAutorizada",true);
        var pagina=service.pagina(consulta);
        assertThat(pagina.opcoes()).isEqualTo(completas);assertThat(pagina.catalogoPendente()).isFalse();
        verify(banco).salvar(consulta,completas);verify(banco).marcarCarregado(consulta,2);
    }
    @Test void escopoMarcadoNaoConsultaFornecedor(){
        var fornecedor=mock(ParallelumClient.class);
        when(banco.escopoCarregado(consulta)).thenReturn(true);
        ReflectionTestUtils.setField(service,"fornecedor",fornecedor);
        ReflectionTestUtils.setField(service,"persistenciaAutorizada",true);
        assertThat(service.pagina(consulta).opcoes()).isEqualTo(marcas);
        verifyNoInteractions(fornecedor);
    }
    @Test void falhaExternaAgendaCargaENaoCacheia(){
        var fornecedor=mock(ParallelumClient.class);var fila=mock(FilaSincronizacaoFipe.class);
        when(fornecedor.opcoes(any())).thenThrow(new FipeException(FipeException.Motivo.LIMITE,"cota"));
        when(banco.listar(consulta,0,50)).thenReturn(List.of());
        ReflectionTestUtils.setField(service,"fornecedor",fornecedor);
        ReflectionTestUtils.setField(service,"fila",fila);
        ReflectionTestUtils.setField(service,"persistenciaAutorizada",true);
        assertThat(service.pagina(consulta).catalogoPendente()).isTrue();
        verify(fila).enviar(eq("338:cars"),anyString());
        verify(banco,never()).marcarCarregado(any(),anyInt());
        verify(valores,never()).set(contains(":pagina:"),anyString(),any(Duration.class));
    }
    @Test void consultaSimultaneaCompartilhaResultado()throws Exception {
        var entrou=new CountDownLatch(1);var liberar=new CountDownLatch(1);
        when(banco.listar(consulta,0,50)).thenAnswer(i->{entrou.countDown();liberar.await(3,TimeUnit.SECONDS);return marcas;});
        var pool=Executors.newFixedThreadPool(2);
        try {
            var primeiro=pool.submit(()->service.pagina(consulta));assertThat(entrou.await(2,TimeUnit.SECONDS)).isTrue();
            var segundo=pool.submit(()->service.pagina(consulta));
            Thread.sleep(100);liberar.countDown();
            assertThat(primeiro.get(3,TimeUnit.SECONDS)).isEqualTo(segundo.get(3,TimeUnit.SECONDS));
            verify(banco,times(1)).listar(consulta,0,50);
        } finally {liberar.countDown();pool.shutdownNow();}
    }
}
