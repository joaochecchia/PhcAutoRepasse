package repasse.phcauto.backend.infra.database.entity.identidade;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.DadosCompraPj;

@Entity
@Table(name = "dados_compra_pj", schema = "identidade")
@Access(AccessType.FIELD)
public class DadosCompraPjEntity extends DadosCompraPj {
    @Id
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "inscricao_estadual", nullable = true, length = 30)
    private String inscricaoEstadual;

    @Column(name = "regime_tributario", nullable = true, length = 80)
    private String regimeTributario;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected DadosCompraPjEntity() { }

    public static DadosCompraPjEntity criar(DadosCompraPj dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new DadosCompraPjEntity();
        entity.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        entity.inscricaoEstadual = dados.getInscricaoEstadual();
        entity.regimeTributario = dados.getRegimeTributario();
        return entity;
    }

    public void atualizar(DadosCompraPj dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(usuarioId, dados.getUsuarioId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.inscricaoEstadual = dados.getInscricaoEstadual();
        this.regimeTributario = dados.getRegimeTributario();
    }

    @Override
    public UUID getUsuarioId() { return usuarioId; }

    @Override
    public String getInscricaoEstadual() { return inscricaoEstadual; }

    @Override
    public String getRegimeTributario() { return regimeTributario; }

    public Long getLockVersion() { return lockVersion; }
}
