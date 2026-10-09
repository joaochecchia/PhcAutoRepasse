package repasse.phcauto.backend.usuarios.internal.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity.RefreshTokenSessionEntity;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.RefreshTokenSessionWriteRepository;
import repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.repository.write.UsuarioWriteRepository;

@Service
public class RefreshTokenService {
    private final RefreshTokenSessionWriteRepository sessions;
    private final UsuarioWriteRepository usuarios;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final long ttl;

    public RefreshTokenService(RefreshTokenSessionWriteRepository sessions, UsuarioWriteRepository usuarios,
            Clock applicationClock, @Value("${REFRESH_TOKEN_TTL_SECONDS:2592000}") long ttl) {
        if (ttl < 900 || ttl > 7776000) throw new IllegalArgumentException(
                "REFRESH_TOKEN_TTL_SECONDS deve estar entre 900 e 7776000");
        this.sessions = sessions;
        this.usuarios = usuarios;
        this.clock = applicationClock;
        this.ttl = ttl;
    }

    @Transactional
    public RefreshEmitido iniciar(UUID usuarioId) {
        return emitir(UUID.randomUUID(), usuarioId, clock.instant().plusSeconds(ttl));
    }

    @Transactional(noRollbackFor = {RefreshTokenInvalidoException.class, RefreshTokenReutilizadoException.class})
    public RefreshRotacionado rotacionar(String token) {
        var agora = clock.instant();
        var atual = sessions.buscarParaAtualizar(hash(token)).orElseThrow(RefreshTokenInvalidoException::new);
        if (atual.getUsadoEm() != null || atual.getRevogadoEm() != null) {
            sessions.revogarFamilia(atual.getFamiliaId(), agora);
            throw new RefreshTokenReutilizadoException();
        }
        if (!atual.getExpiraEm().isAfter(agora)) {
            atual.revogar(agora);
            throw new RefreshTokenInvalidoException();
        }
        var usuario = usuarios.findById(atual.getUsuarioId())
                .filter(u -> u.getAtivo()).orElseThrow(RefreshTokenInvalidoException::new);
        atual.usar(agora);
        var novo = emitir(atual.getFamiliaId(), atual.getUsuarioId(), atual.getExpiraEm());
        return new RefreshRotacionado(usuario.getId(), usuario.getPapel().name(), novo);
    }

    @Transactional
    public void revogar(String token) {
        if (token == null || token.isBlank()) return;
        sessions.buscarParaAtualizar(hash(token))
                .ifPresent(s -> sessions.revogarFamilia(s.getFamiliaId(), clock.instant()));
    }

    private RefreshEmitido emitir(UUID familia, UUID usuario, Instant expiracao) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.save(RefreshTokenSessionEntity.criar(familia, usuario, hash(token), clock.instant(), expiracao));
        return new RefreshEmitido(token, Math.max(0, Duration.between(clock.instant(), expiracao).toSeconds()));
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public record RefreshEmitido(String token, long expiresIn) { }
    public record RefreshRotacionado(UUID usuarioId, String papel, RefreshEmitido refresh) { }
    public static class RefreshTokenInvalidoException extends RuntimeException { }
    public static class RefreshTokenReutilizadoException extends RuntimeException { }
}
