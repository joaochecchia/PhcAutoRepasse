package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import tools.jackson.databind.json.JsonMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import repasse.phcauto.backend.fipe.internal.core.domain.ConsultaExterna;
import repasse.phcauto.backend.fipe.internal.core.domain.ConsultaOpcoesFipe;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
class FipeHttpResilienceTests {
    StringRedisTemplate redis;
    ValueOperations<String,String> valores;
    HttpClient http;
    ParallelumClient client;
    final ConsultaExterna consulta=new ConsultaExterna("cars","25","123","2022-1",338,LocalDate.of(2026,10,1));
    @BeforeEach @SuppressWarnings("unchecked") void preparar(){
        redis=mock(StringRedisTemplate.class);valores=mock(ValueOperations.class);http=mock(HttpClient.class);
        when(redis.opsForValue()).thenReturn(valores);when(redis.hasKey(anyString())).thenReturn(false);
        when(valores.setIfAbsent(anyString(),anyString(),any(Duration.class))).thenReturn(true);
        when(redis.execute(any(RedisScript.class),anyList(),any(),any(),any())).thenReturn(1L);
        client=new ParallelumClient(redis,JsonMapper.builder().findAndAddModules().build(),new SimpleMeterRegistry(),Clock.systemUTC(),"token-ficticio",900,http);
    }
    @Test void redisIndisponivelBloqueiaHttp(){
        when(valores.get(anyString())).thenThrow(new IllegalStateException("offline"));
        assertThatThrownBy(()->client.consultar(consulta)).isInstanceOfSatisfying(FipeException.class,e->assertThat(e.motivo()).isEqualTo(FipeException.Motivo.INDISPONIVEL));
        verifyNoInteractions(http);
    }
    @Test void cotaETravaImpedemHttp(){
        when(redis.execute(any(RedisScript.class),anyList(),any(),any(),any())).thenReturn(0L);
        assertThatThrownBy(()->client.consultar(consulta)).isInstanceOfSatisfying(FipeException.class,e->assertThat(e.motivo()).isEqualTo(FipeException.Motivo.LIMITE));
        verifyNoInteractions(http);
        when(valores.setIfAbsent(anyString(),anyString(),any(Duration.class))).thenReturn(false);
        assertThatThrownBy(()->client.consultar(consulta)).isInstanceOf(FipeException.class);verifyNoInteractions(http);
    }
    @Test @SuppressWarnings("unchecked") void resposta429RespeitaRetryAfterSemCacheNegativo()throws Exception{
        HttpResponse<byte[]> response=mock(HttpResponse.class);when(response.statusCode()).thenReturn(429);
        when(response.headers()).thenReturn(HttpHeaders.of(Map.of("Retry-After",List.of("120")),(a,b)->true));
        when(http.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(response);
        assertThatThrownBy(()->client.consultar(consulta)).isInstanceOfSatisfying(FipeException.class,e->assertThat(e.motivo()).isEqualTo(FipeException.Motivo.LIMITE));
        verify(valores).set(endsWith("cooldown"),eq("1"),eq(Duration.ofSeconds(120)));
        verify(valores,never()).set(anyString(),eq("404"),any(Duration.class));
    }
    @Test @SuppressWarnings("unchecked") void timeoutNaoSeTornaModeloInexistente()throws Exception{
        when(http.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenThrow(new HttpTimeoutException("timeout"));
        assertThatThrownBy(()->client.consultar(consulta)).isInstanceOfSatisfying(FipeException.class,e->assertThat(e.motivo()).isEqualTo(FipeException.Motivo.INDISPONIVEL));
        verify(valores).set(endsWith("cooldown"),eq("1"),eq(Duration.ofSeconds(10)));
        verify(valores,never()).set(anyString(),eq("404"),any(Duration.class));
    }
    @Test @SuppressWarnings("unchecked") void requisicaoSemRespostaRepeteNaHoraSemCooldown()throws Exception{
        HttpResponse<byte[]> response=mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("[{\"code\":\"29\",\"name\":\"Jeep\"}]".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(http.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class)))
            .thenThrow(new HttpTimeoutException("timeout")).thenReturn(response);
        var opcoes=client.opcoes(new ConsultaOpcoesFipe("cars",null,null,338,"",0,50));
        assertThat(opcoes).hasSize(1);
        verify(http,times(2)).send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class));
        verify(redis,times(2)).execute(any(RedisScript.class),anyList(),any(),any(),any());
        verify(valores,never()).set(endsWith("cooldown"),anyString(),any(Duration.class));
    }
    @Test @SuppressWarnings("unchecked") void niveisPorAnoUsamOsEnderecosDeAnosDaMarca()throws Exception{
        HttpResponse<byte[]> response=mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("[{\"code\":\"2020-5\",\"name\":\"2020 Flex\"}]".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(http.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(response);
        client.opcoes(ConsultaOpcoesFipe.anosDaMarca("cars","21",338));
        client.opcoes(ConsultaOpcoesFipe.modelosDoAno("cars","21","2020-5",338));
        var pedidos=org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(http,times(2)).send(pedidos.capture(),any(HttpResponse.BodyHandler.class));
        assertThat(pedidos.getAllValues()).extracting(r->r.uri().toString()).containsExactly(
            "https://fipe.parallelum.com.br/api/v2/cars/brands/21/years?reference=338",
            "https://fipe.parallelum.com.br/api/v2/cars/brands/21/years/2020-5/models?reference=338");
    }
    @Test void ausenciaConfirmadaEmCacheNaoConsomeCota(){
        when(valores.get(contains("cache:"))).thenReturn("404");
        assertThatThrownBy(()->client.consultar(consulta)).isInstanceOfSatisfying(FipeException.class,e->assertThat(e.motivo()).isEqualTo(FipeException.Motivo.NAO_ENCONTRADA));
        verifyNoInteractions(http);verify(valores,never()).setIfAbsent(anyString(),anyString(),any(Duration.class));
    }
    @Test @SuppressWarnings("unchecked") void aceitaMesDaListaDeReferenciasNoFormatoAtualDaParallelum() throws Exception {
        HttpResponse<byte[]> response=mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("[{\"code\":\"338\",\"month\":\"outubro/2026\"}]".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(http.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(response);
        var opcoes=client.opcoes(new ConsultaOpcoesFipe(null,null,null,null,"",0,50));
        assertThat(opcoes).hasSize(1);
        assertThat(opcoes.get(0).mesReferencia()).isEqualTo(LocalDate.of(2026,10,1));
    }
}
