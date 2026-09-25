package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.VeiculoCaracteristica;
import repasse.phcauto.backend.infra.database.entity.catalogo.VeiculoCaracteristicaJpaId;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "veiculo_caracteristicas", schema = "catalogo")
@Access(AccessType.FIELD)
public class VeiculoCaracteristicaEntity extends VeiculoCaracteristica {

    @EmbeddedId
    private VeiculoCaracteristicaJpaId id;

    @Column(name = "observacao", nullable = true, length = 200)
    private String observacao;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected VeiculoCaracteristicaEntity() { }

    public static VeiculoCaracteristicaEntity criar(VeiculoCaracteristica dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new VeiculoCaracteristicaEntity();
        entity.id = new VeiculoCaracteristicaJpaId(dados.getVeiculoId(), dados.getCaracteristicaId());
        entity.observacao = dados.getObservacao();
        return entity;
    }

    public void atualizar(VeiculoCaracteristica dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, new VeiculoCaracteristicaJpaId(dados.getVeiculoId(), dados.getCaracteristicaId()))) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.observacao = dados.getObservacao();
    }

    public VeiculoCaracteristicaJpaId getId() { return id; }

    @Override
    public UUID getVeiculoId() { return id == null ? null : id.veiculoId(); }

    @Override
    public UUID getCaracteristicaId() { return id == null ? null : id.caracteristicaId(); }

    @Override
    public String getObservacao() { return observacao; }

    public Long getLockVersion() { return lockVersion; }
}
