package repasse.phcauto.backend.planos.internal.infrastructure.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.assinaturas.Plano;
import repasse.phcauto.backend.planos.internal.core.NovoPlano;
import repasse.phcauto.backend.planos.internal.core.PlanoDados;

@Entity
@Table(name = "planos", schema = "assinaturas")
@Access(AccessType.FIELD)
public class PlanoEntity extends Plano {
    @Id @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    @Column(name = "valor_centavos")
    private Long valorCentavos;
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "periodo_meses")
    private Integer periodoMeses;
    @Column(name = "limite_anuncios")
    private Integer limiteAnuncios;
    @Column(name = "limite_vistorias_cautelares")
    private Integer limiteVistoriasCautelares;
    @Column(name = "ativo", nullable = false)
    private boolean ativo;
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
    @Version @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected PlanoEntity() { }

    public static PlanoEntity criar(UUID id, NovoPlano plano, Instant criadoEm) {
        var entity = new PlanoEntity();
        entity.id = Objects.requireNonNull(id, "id");
        entity.aplicar(plano.nome(), plano.valorCentavos(), plano.periodoMeses(),
                plano.limiteAnuncios(), plano.limiteVistoriasCautelares(), plano.ativo());
        entity.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm");
        return entity;
    }

    public void atualizar(PlanoDados plano) {
        if (!Objects.equals(id, plano.id())) throw new IllegalArgumentException("A identidade não pode ser alterada");
        aplicar(plano.nome(), plano.valorCentavos(), plano.periodoMeses(), plano.limiteAnuncios(),
                plano.limiteVistoriasCautelares(), plano.ativo());
    }

    private void aplicar(String nome, Long valor, Integer periodo, Integer limite,
            Integer limiteVistorias, boolean ativo) {
        this.nome = Objects.requireNonNull(nome, "nome");
        this.valorCentavos = valor;
        this.periodoMeses = periodo;
        this.limiteAnuncios = limite;
        this.limiteVistoriasCautelares = limiteVistorias;
        this.ativo = ativo;
    }

    public PlanoDados snapshot() {
        return new PlanoDados(id, nome, valorCentavos, periodoMeses, limiteAnuncios,
                limiteVistoriasCautelares, ativo, criadoEm);
    }
    @Override public UUID getId() { return id; }
    @Override public String getNome() { return nome; }
    @Override public Long getValorCentavos() { return valorCentavos; }
    @Override public Integer getPeriodoMeses() { return periodoMeses; }
    @Override public Integer getLimiteAnuncios() { return limiteAnuncios; }
    @Override public Integer getLimiteVistoriasCautelares() { return limiteVistoriasCautelares; }
    @Override public boolean getAtivo() { return ativo; }
    @Override public Instant getCriadoEm() { return criadoEm; }
    public Long getLockVersion() { return lockVersion; }
}
