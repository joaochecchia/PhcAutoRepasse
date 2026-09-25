package repasse.phcauto.backend.infra.database.entity.assinaturas;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.assinaturas.Plano;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "planos", schema = "assinaturas")
@Access(AccessType.FIELD)
public class PlanoEntity extends Plano {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "valor_centavos", nullable = true)
    private Long valorCentavos;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "periodo_meses", nullable = true)
    private Integer periodoMeses;

    @Column(name = "limite_anuncios", nullable = true)
    private Integer limiteAnuncios;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected PlanoEntity() { }

    public static PlanoEntity criar(Plano dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new PlanoEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.nome = Objects.requireNonNull(dados.getNome(), "nome");
        entity.valorCentavos = dados.getValorCentavos();
        entity.periodoMeses = dados.getPeriodoMeses();
        entity.limiteAnuncios = dados.getLimiteAnuncios();
        entity.ativo = dados.getAtivo();
        entity.criadoEm = Objects.requireNonNull(dados.getCriadoEm(), "criadoEm");
        return entity;
    }

    public void atualizar(Plano dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.nome = Objects.requireNonNull(dados.getNome(), "nome");
        this.valorCentavos = dados.getValorCentavos();
        this.periodoMeses = dados.getPeriodoMeses();
        this.limiteAnuncios = dados.getLimiteAnuncios();
        this.ativo = dados.getAtivo();
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public String getNome() { return nome; }

    @Override
    public Long getValorCentavos() { return valorCentavos; }

    @Override
    public Integer getPeriodoMeses() { return periodoMeses; }

    @Override
    public Integer getLimiteAnuncios() { return limiteAnuncios; }

    @Override
    public boolean getAtivo() { return ativo; }

    @Override
    public Instant getCriadoEm() { return criadoEm; }

    public Long getLockVersion() { return lockVersion; }
}
