package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;

@Service
public class SocialRegistrationTicketService {
    private static final String PREFIX = "phcauto:auth:social-registration:";
    private final StringRedisTemplate redis;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();

    public SocialRegistrationTicketService(StringRedisTemplate redis,
            @Value("${app.security.oauth2.registration-ttl:10m}") Duration ttl) {
        this.redis = redis;
        this.ttl = ttl;
    }

    public String criar(ProvedorAutenticacao provedor, String subject, String email, String nome) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String key = chave(token);
        redis.opsForHash().putAll(key, Map.of("provedor", provedor.name(), "subject", subject,
                "email", email, "nome", nome));
        redis.expire(key, ttl);
        return token;
    }

    public Dados buscar(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw new TicketInvalidoException();
        Map<Object, Object> values = redis.opsForHash().entries(chave(token));
        if (values.isEmpty()) throw new TicketInvalidoException();
        try {
            return new Dados(ProvedorAutenticacao.valueOf((String) values.get("provedor")),
                    (String) values.get("subject"), (String) values.get("email"), (String) values.get("nome"));
        } catch (RuntimeException error) { throw new TicketInvalidoException(); }
    }

    public void consumir(String token) { redis.delete(chave(token)); }

    private static String chave(String token) {
        try {
            return PREFIX + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }

    public record Dados(ProvedorAutenticacao provedor, String subject, String email, String nome) { }
    public static class TicketInvalidoException extends RuntimeException { }
}
