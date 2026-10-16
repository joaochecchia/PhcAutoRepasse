package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.persistence;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import repasse.phcauto.backend.fipe.ReferenciaFipe;
import repasse.phcauto.backend.fipe.internal.core.domain.DadosReferencia;
import repasse.phcauto.backend.fipe.internal.core.gateway.CatalogoFipeGateway;
@Component @Primary
public class CachedReferenciaFipeGateway implements CatalogoFipeGateway {
    private final JpaCatalogoFipeGateway banco;
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final MeterRegistry metrics;
    private final Duration ttl;
    public CachedReferenciaFipeGateway(JpaCatalogoFipeGateway banco,StringRedisTemplate redis,ObjectMapper json,MeterRegistry metrics,
            @Value("${app.fipe.prices-cache-ttl:PT10M}")Duration ttl){this.banco=banco;this.redis=redis;this.json=json;this.metrics=metrics;this.ttl=ttl;}
    // Leituras por ID usadas por regras de negócio sempre consultam a fonte transacional.
    public Optional<ReferenciaFipe> buscar(UUID id){return banco.buscar(id);}
    public Optional<ReferenciaFipe> buscarConsulta(String chave,LocalDate mes){
        return cached(chave+":"+mes,()->banco.buscarConsulta(chave,mes));
    }
    private Optional<ReferenciaFipe> cached(String chave,Supplier<Optional<ReferenciaFipe>> consulta){
        String key="phcauto:fipe:v1:preco:"+chave;
        try {String valor=redis.opsForValue().get(key);if(valor!=null){metrics.counter("phcauto.fipe.price.cache","resultado","hit").increment();return Optional.of(json.readValue(valor,ReferenciaFipe.class));}}
        catch(RuntimeException ignored){}
        metrics.counter("phcauto.fipe.price.cache","resultado","miss").increment();
        var resultado=consulta.get();
        resultado.ifPresent(r->{try{redis.opsForValue().set(key,json.writeValueAsString(r),ttl);}catch(RuntimeException ignored){}});
        return resultado;
    }
    public ReferenciaFipe salvarConsulta(DadosReferencia d,String chave){
        var r=banco.salvarConsulta(d,chave);
        try {redis.delete("phcauto:fipe:v1:preco:"+chave+":"+d.mesReferencia());}catch(RuntimeException ignored){}
        return r;
    }
    public List<ReferenciaFipe> pesquisar(String tipo,String marca,String termo,Integer ano,LocalDate mes,int pagina,int tamanho){return banco.pesquisar(tipo,marca,termo,ano,mes,pagina,tamanho);}
}
