package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http;

import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import tools.jackson.databind.*;
import io.micrometer.core.instrument.MeterRegistry;
import repasse.phcauto.backend.fipe.internal.core.domain.*;
import repasse.phcauto.backend.fipe.internal.core.gateway.FornecedorFipeGateway;
import repasse.phcauto.backend.fipe.internal.core.exception.FipeException;
import static repasse.phcauto.backend.fipe.internal.core.exception.FipeException.Motivo.*;

/** Todas as chamadas externas são limitadas pela mesma credencial, inclusive opções do catálogo. */
@Component
public class ParallelumClient implements FornecedorFipeGateway, repasse.phcauto.backend.fipe.internal.core.gateway.OpcoesFipeGateway {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(ParallelumClient.class);
    private static final RedisScript<Long> COTA = RedisScript.of("""
        redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', ARGV[1] - 86400000)
        if redis.call('ZCARD', KEYS[1]) >= tonumber(ARGV[2]) then return 0 end
        redis.call('ZADD', KEYS[1], ARGV[1], ARGV[3])
        redis.call('EXPIRE', KEYS[1], 86401)
        return 1
        """,Long.class);
    private static final RedisScript<Long> UNLOCK = RedisScript.of("""
        if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) end
        return 0
        """,Long.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final MeterRegistry metrics;
    private final Clock clock;
    private final String token, prefix;
    private final int limite;
    private final HttpClient http;
    private Duration requestTimeout=Duration.ofSeconds(5);
    // Timeout ou falha de rede costuma ser lentidão momentânea: pausa curta para não travar todo o catálogo.
    @Value("${app.fipe.failure-cooldown:PT10S}") private Duration cooldownFalha=Duration.ofSeconds(10);
    @org.springframework.beans.factory.annotation.Autowired
    public ParallelumClient(StringRedisTemplate redis,ObjectMapper json,MeterRegistry metrics,Clock clock,
            @Value("${app.fipe.parallelum-token:}")String token,
            @Value("${app.fipe.daily-limit:900}")int limite,
            @Value("${app.fipe.connect-timeout:PT2S}")Duration connectTimeout,
            @Value("${app.fipe.request-timeout:PT10S}")Duration requestTimeout) {
        // HTTP/1.1: no HTTP/2 todas as consultas dividem uma conexão; se ela trava após um timeout, o cliente
        // continua reutilizando-a e toda chamada externa falha até o restart. No HTTP/1.1 a conexão com timeout é descartada.
        this(redis,json,metrics,clock,token,limite,HttpClient.newBuilder().connectTimeout(connectTimeout).version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER).build());
        if(requestTimeout.isNegative() || requestTimeout.isZero() || requestTimeout.compareTo(Duration.ofSeconds(30))>0)throw new IllegalArgumentException("Timeout FIPE inválido");
        this.requestTimeout=requestTimeout;
    }
    ParallelumClient(StringRedisTemplate redis,ObjectMapper json,MeterRegistry metrics,Clock clock,String token,int limite,HttpClient http) {
        this.http=http;
        this.redis=redis;this.json=json;this.metrics=metrics;this.clock=clock;this.token=token;
        if(limite<1 || limite>1000)throw new IllegalArgumentException("Limite FIPE deve estar entre 1 e 1000");
        this.limite=limite;this.prefix="phcauto:fipe:parallelum:"+hash(token)+":";
    }
    @Override public DadosReferencia consultar(ConsultaExterna c) {
        var n=obter(c.tipo()+"/brands/"+c.marcaId()+"/models/"+c.modeloId()+"/years/"+c.anoId()+"?reference="+c.referencia());
        try {
            var dados=converter(n);
            String tipo=switch(c.tipo()){case "cars"->"CARRO";case "motorcycles"->"MOTO";default->"CAMINHAO";};
            if(!tipo.equals(dados.tipo()) || dados.anoModelo()!=Integer.parseInt(c.anoId().split("-")[0])
                    || !dados.mesReferencia().equals(c.mesReferencia()))
                throw new IllegalArgumentException("Resposta incompatível");
            return dados;
        } catch(RuntimeException e) { throw new FipeException(INDISPONIVEL,"Resposta inválida do fornecedor FIPE"); }
    }
    public static DadosReferencia converter(JsonNode n) {
        String tipo=switch(n.path("vehicleType").asInt()){case 1->"CARRO";case 2->"MOTO";case 3->"CAMINHAO";default->throw new IllegalArgumentException("Tipo desconhecido");};
        String preco=n.path("price").asText();
        if(!preco.matches("R\\$\\s*[0-9.]+,[0-9]{2}"))throw new IllegalArgumentException("Preço inválido");
        long centavos=new BigDecimal(preco.replace("R$","").trim().replace(".","").replace(",",".")).movePointRight(2).longValueExact();
        var mes=converterMes(n.path("referenceMonth").asText());
        return new DadosReferencia(n.path("codeFipe").asText(),tipo,n.path("brand").asText(),n.path("model").asText(),
                n.path("modelYear").asInt(),n.path("fuel").asText(),mes,centavos);
    }
    @Override public List<OpcaoFipe> opcoes(ConsultaOpcoesFipe c) {
        var resultado=opcoesJson(c.tipo(),c.marcaId(),c.modeloId(),c.referencia());
        if(!resultado.isArray())throw new FipeException(INDISPONIVEL,"Catálogo externo inválido");
        var opcoes=new ArrayList<OpcaoFipe>();
        for(var n:resultado) {
            String nome=n.path(c.tipo()==null?"month":"name").asText();
            opcoes.add(new OpcaoFipe(n.path("code").asText(),nome,c.tipo()==null?converterMes(nome):null));
        }
        return List.copyOf(opcoes);
    }
    private static LocalDate converterMes(String valor) {
        for(String padrao:List.of("MMMM/uuuu","MMMM 'de' uuuu")) {
            try {
                var formato=new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(padrao)
                    .toFormatter(Locale.forLanguageTag("pt-BR"));
                return YearMonth.parse(valor.trim(),formato).atDay(1);
            } catch(DateTimeParseException ignorada) { /* O fornecedor usa ambos os formatos conforme o endpoint. */ }
        }
        throw new IllegalArgumentException("Mês FIPE inválido");
    }
    private JsonNode opcoesJson(String tipo,String marca,String modelo,Integer referencia) {
        if(tipo==null) return obter("references");
        if(!Set.of("cars","motorcycles","trucks").contains(tipo) || referencia==null || referencia<1
                || (marca!=null&&!marca.matches("[0-9]{1,10}")) || (modelo!=null&&!modelo.matches("[0-9]{1,10}"))
                || (modelo!=null&&marca==null))throw new FipeException(INVALIDA,"Filtros do catálogo externo inválidos");
        String caminho=tipo+"/brands"+(marca==null?"":"/"+marca+"/models"+(modelo==null?"":"/"+modelo+"/years"));
        return obter(caminho+"?reference="+referencia);
    }
    private JsonNode obter(String caminho) {
        if(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("Consulta HTTP FIPE não pode ocorrer dentro de transação");
        if(token.isBlank())throw new FipeException(INDISPONIVEL,"Consulta externa FIPE não configurada");
        // Trava por recurso: consultas a recursos diferentes não se bloqueiam; a mesma consulta não é repetida.
        String cache=prefix+"cache:"+hash(caminho), lock=prefix+"lock:"+hash(caminho), dono=UUID.randomUUID().toString();
        boolean adquirido=false;
        try {
            String valor=redis.opsForValue().get(cache);
            if("404".equals(valor))throw new FipeException(NAO_ENCONTRADA,"Referência não encontrada no fornecedor");
            if(valor!=null){metrics.counter("phcauto.fipe.external","resultado","cache").increment();return json.readTree(valor);}
            if(Boolean.TRUE.equals(redis.hasKey(prefix+"cooldown")))throw new FipeException(INDISPONIVEL,"Fornecedor FIPE temporariamente indisponível");
            adquirido=Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(lock,dono,Duration.ofSeconds(45)));
            if(!adquirido) {
                // Outra requisição já busca este recurso: aguarda o resultado dela em vez de falhar imediatamente.
                for(int i=0;i<20 && valor==null;i++){Thread.sleep(100);valor=redis.opsForValue().get(cache);}
                if("404".equals(valor))throw new FipeException(NAO_ENCONTRADA,"Referência não encontrada no fornecedor");
                if(valor!=null)return json.readTree(valor);
                throw new FipeException(INDISPONIVEL,"Consulta FIPE em andamento; tente novamente em instantes");
            }
            valor=redis.opsForValue().get(cache);
            if("404".equals(valor))throw new FipeException(NAO_ENCONTRADA,"Referência não encontrada no fornecedor");
            if(valor!=null)return json.readTree(valor);
            Long permitido=redis.execute(COTA,List.of(prefix+"cota"),Long.toString(clock.millis()),Integer.toString(limite),dono);
            if(!Long.valueOf(1).equals(permitido))throw new FipeException(LIMITE,"Cota diária FIPE atingida; use o catálogo local");
            var request=HttpRequest.newBuilder(URI.create("https://fipe.parallelum.com.br/api/v2/"+caminho))
                .timeout(requestTimeout).header("X-Subscription-Token",token).header("Accept","application/json").GET().build();
            var inicio=System.nanoTime();
            var resposta=http.send(request,info->new RespostaLimitadaFipe(2_000_000));
            metrics.timer("phcauto.fipe.http.duration").record(System.nanoTime()-inicio,java.util.concurrent.TimeUnit.NANOSECONDS);
            int status=resposta.statusCode();
            metrics.counter("phcauto.fipe.external","resultado",Integer.toString(status)).increment();
            if(status==404){redis.opsForValue().set(cache,"404",Duration.ofMinutes(30));throw new FipeException(NAO_ENCONTRADA,"Referência não encontrada no fornecedor");}
            if(status!=200){
                long espera=status==429?retryAfter(resposta):60;
                LOG.warn("event=fipe_external_http_error status={} cooldown_s={} path={}",status,espera,caminho.split("\\?")[0]);
                redis.opsForValue().set(prefix+"cooldown","1",Duration.ofSeconds(espera));
                throw new FipeException(status==429?LIMITE:INDISPONIVEL,"Fornecedor FIPE temporariamente indisponível");
            }
            if(resposta.body().length>2_000_000)throw new FipeException(INDISPONIVEL,"Resposta FIPE excedeu o limite");
            valor=new String(resposta.body(),StandardCharsets.UTF_8);
            var resultado=json.readTree(valor);
            if(resultado.isArray()) {
                for(var item:resultado) {
                    String codigo=item.path("code").asText(),nome=item.path(caminho.equals("references")?"month":"name").asText();
                    if(!codigo.matches("[0-9]{1,10}(-[0-9])?") || nome.isBlank() || nome.length()>250)
                        throw new FipeException(INDISPONIVEL,"Resposta de catálogo inválida");
                }
            } else if(caminho.contains("/years/")) {converter(resultado);}
            else throw new FipeException(INDISPONIVEL,"Resposta de catálogo inválida");
            if(!resultado.isArray() || !resultado.isEmpty())redis.opsForValue().set(cache,valor,Duration.ofHours(6));
            return resultado;
        } catch(FipeException e){throw e;}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new FipeException(INDISPONIVEL,"Consulta FIPE interrompida");}
        catch(Exception e){
            LOG.warn("event=fipe_external_failure error={} path={}",e.getClass().getSimpleName(),caminho.split("\\?")[0]);
            try { redis.opsForValue().set(prefix+"cooldown","1",cooldownFalha); }
            catch(RuntimeException ignored) { /* Redis indisponível: próximas chamadas externas também falham fechadas. */ }
            metrics.counter("phcauto.fipe.external","resultado","falha").increment();throw new FipeException(INDISPONIVEL,"Consulta externa FIPE indisponível; catálogo local preservado");}
        finally {if(adquirido)try{redis.execute(UNLOCK,List.of(lock),dono);}catch(RuntimeException ignored){/* A trava expira em 15 segundos. */}}
    }
    private long retryAfter(HttpResponse<?> resposta){
        String valor=resposta.headers().firstValue("Retry-After").orElse("60");
        try{return Math.max(1,Math.min(86400,Long.parseLong(valor)));}catch(NumberFormatException e){
            try{return Math.max(1,Math.min(86400,Duration.between(clock.instant(),ZonedDateTime.parse(valor,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()).toSeconds()));}
            catch(RuntimeException ignored){return 60;}
        }
    }
    private static String hash(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
