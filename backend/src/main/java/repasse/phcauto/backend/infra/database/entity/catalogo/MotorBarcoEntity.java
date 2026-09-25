package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.catalogo.MotorBarco;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "motores_barco", schema = "catalogo")
@Access(AccessType.FIELD)
public class MotorBarcoEntity extends MotorBarco {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "barco_id", nullable = false)
    private UUID barcoId;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "posicao", nullable = false)
    private int posicao;

    @Column(name = "fabricante", nullable = true, length = 100)
    private String fabricante;

    @Column(name = "modelo", nullable = true, length = 120)
    private String modelo;

    @Column(name = "potencia_hp", nullable = true, precision = 8, scale = 2)
    private BigDecimal potenciaHp;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "ano", nullable = true)
    private Integer ano;

    @Column(name = "horas_uso", nullable = true)
    private Integer horasUso;

    @Column(name = "horas_desde_revisao", nullable = true)
    private Integer horasDesdeRevisao;

    @Column(name = "combustivel", nullable = true, length = 60)
    private String combustivel;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected MotorBarcoEntity() { }

    public static MotorBarcoEntity criar(MotorBarco dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new MotorBarcoEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.barcoId = Objects.requireNonNull(dados.getBarcoId(), "barcoId");
        entity.posicao = dados.getPosicao();
        entity.fabricante = dados.getFabricante();
        entity.modelo = dados.getModelo();
        entity.potenciaHp = dados.getPotenciaHp();
        entity.ano = dados.getAno();
        entity.horasUso = dados.getHorasUso();
        entity.horasDesdeRevisao = dados.getHorasDesdeRevisao();
        entity.combustivel = dados.getCombustivel();
        return entity;
    }

    public void atualizar(MotorBarco dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.barcoId = Objects.requireNonNull(dados.getBarcoId(), "barcoId");
        this.posicao = dados.getPosicao();
        this.fabricante = dados.getFabricante();
        this.modelo = dados.getModelo();
        this.potenciaHp = dados.getPotenciaHp();
        this.ano = dados.getAno();
        this.horasUso = dados.getHorasUso();
        this.horasDesdeRevisao = dados.getHorasDesdeRevisao();
        this.combustivel = dados.getCombustivel();
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public UUID getBarcoId() { return barcoId; }

    @Override
    public int getPosicao() { return posicao; }

    @Override
    public String getFabricante() { return fabricante; }

    @Override
    public String getModelo() { return modelo; }

    @Override
    public BigDecimal getPotenciaHp() { return potenciaHp; }

    @Override
    public Integer getAno() { return ano; }

    @Override
    public Integer getHorasUso() { return horasUso; }

    @Override
    public Integer getHorasDesdeRevisao() { return horasDesdeRevisao; }

    @Override
    public String getCombustivel() { return combustivel; }

    public Long getLockVersion() { return lockVersion; }
}
