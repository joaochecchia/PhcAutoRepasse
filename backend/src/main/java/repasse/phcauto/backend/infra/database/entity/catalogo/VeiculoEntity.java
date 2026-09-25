package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.catalogo.TipoVeiculo;
import repasse.phcauto.backend.domain.model.catalogo.Veiculo;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "veiculos", schema = "catalogo")
@Access(AccessType.FIELD)
public class VeiculoEntity extends Veiculo {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proprietario_id", nullable = false)
    private UUID proprietarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 32)
    private TipoVeiculo tipo;

    @Column(name = "fabricante", nullable = false, length = 100)
    private String fabricante;

    @Column(name = "modelo", nullable = false, length = 120)
    private String modelo;

    @Column(name = "versao", nullable = true, length = 180)
    private String versao;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "ano_fabricacao", nullable = true)
    private Integer anoFabricacao;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "ano_modelo", nullable = true)
    private Integer anoModelo;

    @Column(name = "cor", nullable = true, length = 60)
    private String cor;

    @Column(name = "identificador_publico", nullable = true, length = 80)
    private String identificadorPublico;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected VeiculoEntity() { }

    public static VeiculoEntity criar(Veiculo dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new VeiculoEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.proprietarioId = Objects.requireNonNull(dados.getProprietarioId(), "proprietarioId");
        entity.tipo = Objects.requireNonNull(dados.getTipo(), "tipo");
        entity.fabricante = Objects.requireNonNull(dados.getFabricante(), "fabricante");
        entity.modelo = Objects.requireNonNull(dados.getModelo(), "modelo");
        entity.versao = dados.getVersao();
        entity.anoFabricacao = dados.getAnoFabricacao();
        entity.anoModelo = dados.getAnoModelo();
        entity.cor = dados.getCor();
        entity.identificadorPublico = dados.getIdentificadorPublico();
        entity.criadoEm = Objects.requireNonNull(dados.getCriadoEm(), "criadoEm");
        entity.atualizadoEm = Objects.requireNonNull(dados.getAtualizadoEm(), "atualizadoEm");
        return entity;
    }

    public void atualizar(Veiculo dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.proprietarioId = Objects.requireNonNull(dados.getProprietarioId(), "proprietarioId");
        this.tipo = Objects.requireNonNull(dados.getTipo(), "tipo");
        this.fabricante = Objects.requireNonNull(dados.getFabricante(), "fabricante");
        this.modelo = Objects.requireNonNull(dados.getModelo(), "modelo");
        this.versao = dados.getVersao();
        this.anoFabricacao = dados.getAnoFabricacao();
        this.anoModelo = dados.getAnoModelo();
        this.cor = dados.getCor();
        this.identificadorPublico = dados.getIdentificadorPublico();
        this.atualizadoEm = Objects.requireNonNull(dados.getAtualizadoEm(), "atualizadoEm");
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public UUID getProprietarioId() { return proprietarioId; }

    @Override
    public TipoVeiculo getTipo() { return tipo; }

    @Override
    public String getFabricante() { return fabricante; }

    @Override
    public String getModelo() { return modelo; }

    @Override
    public String getVersao() { return versao; }

    @Override
    public Integer getAnoFabricacao() { return anoFabricacao; }

    @Override
    public Integer getAnoModelo() { return anoModelo; }

    @Override
    public String getCor() { return cor; }

    @Override
    public String getIdentificadorPublico() { return identificadorPublico; }

    @Override
    public Instant getCriadoEm() { return criadoEm; }

    @Override
    public Instant getAtualizadoEm() { return atualizadoEm; }

    public Long getLockVersion() { return lockVersion; }
}
