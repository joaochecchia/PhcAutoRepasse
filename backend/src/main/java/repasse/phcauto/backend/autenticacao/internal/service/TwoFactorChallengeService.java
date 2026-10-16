package repasse.phcauto.backend.autenticacao.internal.service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import repasse.phcauto.backend.notificacoes.EmailService;
import repasse.phcauto.backend.usuarios.UsuarioSeguranca;
import repasse.phcauto.backend.usuarios.SegurancaUsuarios;

@Service
public class TwoFactorChallengeService {
    private static final RedisScript<Long> VALIDAR=RedisScript.of("""
        if redis.call('EXISTS',KEYS[1]) == 0 then return -1 end
        local tentativas=tonumber(redis.call('HGET',KEYS[1],'tentativas') or '0')
        if tentativas >= tonumber(ARGV[2]) then redis.call('DEL',KEYS[1]); return -2 end
        if redis.call('HGET',KEYS[1],'codigoHash') ~= ARGV[1] then
          redis.call('HINCRBY',KEYS[1],'tentativas',1); return 0
        end
        redis.call('DEL',KEYS[1]); return 1
        """,Long.class);
    private final StringRedisTemplate redis;
    private final EmailService emails;
    private final SegurancaUsuarios usuarios;
    private final SecureRandom random=new SecureRandom();
    private final byte[] secret;
    private final Duration ttl;
    private final int maxTentativas;
    private final boolean enabled;
    private final Duration cooldownReenvio;
    public TwoFactorChallengeService(StringRedisTemplate redis,EmailService emails,SegurancaUsuarios usuarios,
            @Value("${app.security.mfa.enabled:false}") boolean enabled,
            @Value("${app.security.mfa.hmac-secret:}") String secret,
            @Value("${app.security.mfa.ttl:5m}") Duration ttl,
            @Value("${app.security.mfa.max-attempts:5}") int maxTentativas,
            @Value("${app.security.mfa.resend-cooldown:60s}") Duration cooldownReenvio,
            @Value("${app.email.brevo.enabled:false}") boolean brevoEnabled) {
        if(enabled && (!brevoEnabled || secret.isBlank())) throw new IllegalStateException(
                "MFA habilitado exige Brevo habilitada e MFA_HMAC_SECRET em Base64");
        this.redis=redis;this.emails=emails;this.usuarios=usuarios;this.enabled=enabled;this.ttl=ttl;
        this.maxTentativas=maxTentativas;this.cooldownReenvio=cooldownReenvio;
        try { this.secret=secret.isBlank()?new byte[32]:Base64.getDecoder().decode(secret); }
        catch(IllegalArgumentException e){throw new IllegalArgumentException("MFA_HMAC_SECRET deve estar em Base64",e);}
        if(enabled && this.secret.length<32) throw new IllegalArgumentException("MFA_HMAC_SECRET exige 32 bytes");
    }
    public boolean enabled(){return enabled;}
    public Desafio criar(UsuarioSeguranca usuario,String userAgent){
        String id=UUID.randomUUID().toString(); String codigo=String.format("%06d",random.nextInt(1_000_000));
        String key=key(id);
        redis.opsForHash().putAll(key,Map.of("usuarioId",usuario.id().toString(),"papel",usuario.papel(),
                "email",usuario.email(),"versao",Long.toString(usuario.versaoSeguranca()),
                "agenteHash",repasse.phcauto.backend.autenticacao.internal.security.SecurityHashes.sha256(
                        userAgent==null?"desconhecido":userAgent),"codigoHash",hmac(id,codigo),"tentativas","0"));
        redis.expire(key,ttl);
        redis.opsForValue().set(cooldownKey(id),"1",cooldownReenvio);
        try { emails.enviarCodigoSegundoFator(usuario.email(),codigo,Math.max(1,ttl.toMinutes())); }
        catch(RuntimeException e){redis.delete(key);throw e;}
        return new Desafio(id,ttl.toSeconds(),mascarar(usuario.email()));
    }
    public Desafio reenviar(String id,String userAgent){
        if(id==null||!id.matches("[0-9a-fA-F-]{36}")) throw new CodigoInvalidoException();
        String key=key(id);Map<Object,Object> dados=redis.opsForHash().entries(key);
        if(dados.isEmpty()) throw new CodigoInvalidoException();
        String agenteHash=repasse.phcauto.backend.autenticacao.internal.security.SecurityHashes.sha256(
                userAgent==null?"desconhecido":userAgent);
        if(!MessageDigest.isEqual(agenteHash.getBytes(StandardCharsets.UTF_8),
                ((String)dados.get("agenteHash")).getBytes(StandardCharsets.UTF_8))) throw new CodigoInvalidoException();
        Boolean permitido=redis.opsForValue().setIfAbsent(cooldownKey(id),"1",cooldownReenvio);
        if(!Boolean.TRUE.equals(permitido)) throw new ReenvioAguardeException(cooldownReenvio.toSeconds());
        var usuario=usuarios.buscarAtivo(UUID.fromString((String)dados.get("usuarioId")));
        if(usuario.versaoSeguranca()!=Long.parseLong((String)dados.get("versao"))) throw new CodigoInvalidoException();
        var novo=criar(usuario,userAgent);redis.delete(key);return novo;
    }
    public DadosDesafio validar(String id,String codigo){
        if(id==null||codigo==null||!codigo.matches("\\d{6}")) throw new CodigoInvalidoException();
        String key=key(id); Map<Object,Object> dados=redis.opsForHash().entries(key);
        Long result=redis.execute(VALIDAR,List.of(key),hmac(id,codigo),Integer.toString(maxTentativas));
        if(result==null||result!=1||dados.isEmpty()) throw new CodigoInvalidoException();
        return new DadosDesafio(UUID.fromString((String)dados.get("usuarioId")),(String)dados.get("papel"),
                (String)dados.get("email"),Long.parseLong((String)dados.get("versao")),(String)dados.get("agenteHash"));
    }
    private String hmac(String id,String codigo){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret,"HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal((id+":"+codigo).getBytes(StandardCharsets.UTF_8)));}
        catch(GeneralSecurityException e){throw new IllegalStateException(e);}}
    private static String mascarar(String email){int at=email.indexOf('@');return at<=1?"***"+email.substring(Math.max(0,at)):
            email.substring(0,1)+"***"+email.substring(at);}
    private static String key(String id){return "phcauto:auth:2fa:"+id;}
    private static String cooldownKey(String id){return "phcauto:auth:2fa:resend:"+id;}
    public record Desafio(String challengeId,long expiresIn,String emailMascarado){}
    public record DadosDesafio(UUID usuarioId,String papel,String email,long versao,String agenteHash){}
    public static class CodigoInvalidoException extends RuntimeException { }
    public static class ReenvioAguardeException extends RuntimeException {
        private final long segundos;
        public ReenvioAguardeException(long segundos){this.segundos=Math.max(1,segundos);}
        public long segundos(){return segundos;}
    }
}
