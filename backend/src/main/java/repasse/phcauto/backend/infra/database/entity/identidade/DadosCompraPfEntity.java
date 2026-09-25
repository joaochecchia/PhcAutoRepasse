package repasse.phcauto.backend.infra.database.entity.identidade;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.identidade.DadosCompraPf;

@Entity
@Table(name = "dados_compra_pf", schema = "identidade")
@Access(AccessType.FIELD)
public class DadosCompraPfEntity extends DadosCompraPf {
    @Id
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "rg", nullable = true, length = 30)
    private String rg;

    @Column(name = "nome_pai", nullable = true, length = 160)
    private String nomePai;

    @Column(name = "nome_mae", nullable = true, length = 160)
    private String nomeMae;

    @Column(name = "naturalidade", nullable = true, length = 160)
    private String naturalidade;

    @Column(name = "genero", nullable = true, length = 60)
    private String genero;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected DadosCompraPfEntity() { }

    public static DadosCompraPfEntity criar(DadosCompraPf dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new DadosCompraPfEntity();
        entity.usuarioId = Objects.requireNonNull(dados.getUsuarioId(), "usuarioId");
        entity.rg = dados.getRg();
        entity.nomePai = dados.getNomePai();
        entity.nomeMae = dados.getNomeMae();
        entity.naturalidade = dados.getNaturalidade();
        entity.genero = dados.getGenero();
        return entity;
    }

    public void atualizar(DadosCompraPf dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(usuarioId, dados.getUsuarioId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.rg = dados.getRg();
        this.nomePai = dados.getNomePai();
        this.nomeMae = dados.getNomeMae();
        this.naturalidade = dados.getNaturalidade();
        this.genero = dados.getGenero();
    }

    @Override
    public UUID getUsuarioId() { return usuarioId; }

    @Override
    public String getRg() { return rg; }

    @Override
    public String getNomePai() { return nomePai; }

    @Override
    public String getNomeMae() { return nomeMae; }

    @Override
    public String getNaturalidade() { return naturalidade; }

    @Override
    public String getGenero() { return genero; }

    public Long getLockVersion() { return lockVersion; }
}
