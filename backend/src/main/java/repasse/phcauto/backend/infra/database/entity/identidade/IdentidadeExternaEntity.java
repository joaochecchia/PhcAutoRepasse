package repasse.phcauto.backend.infra.database.entity.identidade;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.IdentidadeExterna;
import repasse.phcauto.backend.domain.model.identidade.ProvedorAutenticacao;

@Entity
@Table(name = "identidades_externas", schema = "identidade")
@Access(AccessType.FIELD)
public class IdentidadeExternaEntity extends IdentidadeExterna {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provedor", nullable = false, length = 32)
    private ProvedorAutenticacao provedor;

    @Column(name = "identificador_externo", nullable = false, length = 255)
    private String identificadorExterno;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected IdentidadeExternaEntity() { }

    public static IdentidadeExternaEntity criar(IdentidadeExterna dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new IdentidadeExternaEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        entity.provedor = Objects.requireNonNull(dados.getProvedor(), "provedor");
        entity.identificadorExterno = Objects.requireNonNull(dados.getIdentificadorExterno(), "identificadorExterno");
        return entity;
    }

    public void atualizar(IdentidadeExterna dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        this.provedor = Objects.requireNonNull(dados.getProvedor(), "provedor");
        this.identificadorExterno = Objects.requireNonNull(dados.getIdentificadorExterno(), "identificadorExterno");
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public UUID getUsuarioId() { return usuarioId; }

    @Override
    public ProvedorAutenticacao getProvedor() { return provedor; }

    @Override
    public String getIdentificadorExterno() { return identificadorExterno; }

    public Long getLockVersion() { return lockVersion; }
}
