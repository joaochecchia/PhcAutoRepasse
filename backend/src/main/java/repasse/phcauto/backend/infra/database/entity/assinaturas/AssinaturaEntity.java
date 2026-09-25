package repasse.phcauto.backend.infra.database.entity.assinaturas;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.assinaturas.Assinatura;
import repasse.phcauto.backend.domain.model.assinaturas.StatusAssinatura;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "assinaturas", schema = "assinaturas")
@Access(AccessType.FIELD)
public class AssinaturaEntity extends Assinatura {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "plano_id", nullable = false)
    private UUID planoId;

    @Column(name = "valor_contratado_centavos", nullable = false)
    private long valorContratadoCentavos;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private StatusAssinatura status;

    @Column(name = "inicio_em", nullable = true)
    private Instant inicioEm;

    @Column(name = "fim_em", nullable = true)
    private Instant fimEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected AssinaturaEntity() { }

    public static AssinaturaEntity criar(Assinatura dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new AssinaturaEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        entity.planoId = Objects.requireNonNull(dados.getPlanoId(), "planoId");
        entity.valorContratadoCentavos = dados.getValorContratadoCentavos();
        entity.status = Objects.requireNonNull(dados.getStatus(), "status");
        entity.inicioEm = dados.getInicioEm();
        entity.fimEm = dados.getFimEm();
        entity.criadoEm = Objects.requireNonNull(dados.getCriadoEm(), "criadoEm");
        return entity;
    }

    public void atualizar(Assinatura dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        this.planoId = Objects.requireNonNull(dados.getPlanoId(), "planoId");
        this.valorContratadoCentavos = dados.getValorContratadoCentavos();
        this.status = Objects.requireNonNull(dados.getStatus(), "status");
        this.inicioEm = dados.getInicioEm();
        this.fimEm = dados.getFimEm();
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public UUID getUsuarioId() { return usuarioId; }

    @Override
    public UUID getPlanoId() { return planoId; }

    @Override
    public long getValorContratadoCentavos() { return valorContratadoCentavos; }

    @Override
    public StatusAssinatura getStatus() { return status; }

    @Override
    public Instant getInicioEm() { return inicioEm; }

    @Override
    public Instant getFimEm() { return fimEm; }

    @Override
    public Instant getCriadoEm() { return criadoEm; }

    public Long getLockVersion() { return lockVersion; }
}
