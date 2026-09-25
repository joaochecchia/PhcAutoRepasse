package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Caracteristica;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "caracteristicas", schema = "catalogo")
@Access(AccessType.FIELD)
public class CaracteristicaEntity extends Caracteristica {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_veiculo", nullable = false, length = 32)
    private TipoVeiculo tipoVeiculo;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "grupo", nullable = true, length = 60)
    private String grupo;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected CaracteristicaEntity() { }

    public static CaracteristicaEntity criar(Caracteristica dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new CaracteristicaEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.tipoVeiculo = Objects.requireNonNull(dados.getTipoVeiculo(), "tipoVeiculo");
        entity.nome = Objects.requireNonNull(dados.getNome(), "nome");
        entity.grupo = dados.getGrupo();
        return entity;
    }

    public void atualizar(Caracteristica dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.tipoVeiculo = Objects.requireNonNull(dados.getTipoVeiculo(), "tipoVeiculo");
        this.nome = Objects.requireNonNull(dados.getNome(), "nome");
        this.grupo = dados.getGrupo();
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public TipoVeiculo getTipoVeiculo() { return tipoVeiculo; }

    @Override
    public String getNome() { return nome; }

    @Override
    public String getGrupo() { return grupo; }

    public Long getLockVersion() { return lockVersion; }
}
