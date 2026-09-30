package repasse.phcauto.backend.compliance.internal.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.compliance.internal.core.domain.AceiteTermos;

@Entity
@Table(name = "aceites_termos", schema = "compliance")
@Access(AccessType.FIELD)
public class AceiteTermosEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "aceite_em", nullable = false, updatable = false)
    private Instant aceiteEm;

    @Column(name = "versao_termos_uso", nullable = false, updatable = false, length = 64)
    private String versaoTermosUso;

    @Column(name = "versao_politica_privacidade", nullable = false, updatable = false, length = 64)
    private String versaoPoliticaPrivacidade;

    @Column(name = "endereco_rede", nullable = false, updatable = false, length = 255)
    private String enderecoRede;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private Instant registradoEm;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected AceiteTermosEntity() {
    }

    public static AceiteTermosEntity criar(AceiteTermos aceite) {
        Objects.requireNonNull(aceite, "aceite");
        var entity = new AceiteTermosEntity();
        entity.id = aceite.id();
        entity.usuarioId = aceite.usuarioId();
        entity.aceiteEm = aceite.aceiteEm();
        entity.versaoTermosUso = aceite.versaoTermosUso();
        entity.versaoPoliticaPrivacidade = aceite.versaoPoliticaPrivacidade();
        entity.enderecoRede = aceite.enderecoRede();
        entity.registradoEm = aceite.registradoEm();
        return entity;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public Instant getAceiteEm() { return aceiteEm; }
    public String getVersaoTermosUso() { return versaoTermosUso; }
    public String getVersaoPoliticaPrivacidade() { return versaoPoliticaPrivacidade; }
    public String getEnderecoRede() { return enderecoRede; }
    public Instant getRegistradoEm() { return registradoEm; }
    public Long getLockVersion() { return lockVersion; }
}
