package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import repasse.phcauto.backend.infra.database.sync.WriteOnlyOperationalData;

@Entity
@WriteOnlyOperationalData
@Table(name = "sessoes_refresh_token", schema = "identidade")
@Access(AccessType.FIELD)
public class RefreshTokenSessionEntity {
    @Id private UUID id;
    @Column(name = "familia_id", nullable = false) private UUID familiaId;
    @Column(name = "usuario_id", nullable = false) private UUID usuarioId;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "criado_em", nullable = false) private Instant criadoEm;
    @Column(name = "expira_em", nullable = false) private Instant expiraEm;
    @Column(name = "usado_em") private Instant usadoEm;
    @Column(name = "revogado_em") private Instant revogadoEm;

    protected RefreshTokenSessionEntity() { }

    public static RefreshTokenSessionEntity criar(UUID familiaId, UUID usuarioId, String hash,
            Instant agora, Instant expiracao) {
        var entity = new RefreshTokenSessionEntity();
        entity.id = UUID.randomUUID();
        entity.familiaId = familiaId;
        entity.usuarioId = usuarioId;
        entity.tokenHash = hash;
        entity.criadoEm = agora;
        entity.expiraEm = expiracao;
        return entity;
    }

    public UUID getFamiliaId() { return familiaId; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getExpiraEm() { return expiraEm; }
    public Instant getUsadoEm() { return usadoEm; }
    public Instant getRevogadoEm() { return revogadoEm; }
    public void usar(Instant instante) { usadoEm = instante; }
    public void revogar(Instant instante) { revogadoEm = instante; }
}
