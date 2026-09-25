package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.LinhaAmarela;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "linha_amarela", schema = "catalogo")
@Access(AccessType.FIELD)
public class LinhaAmarelaEntity extends LinhaAmarela {

    @Id
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private UUID veiculoId;

    @Column(name = "tipo_maquina", nullable = true, length = 80)
    private String tipoMaquina;

    @Column(name = "horimetro", nullable = true)
    private Integer horimetro;

    @Column(name = "peso_operacional_kg", nullable = true)
    private Integer pesoOperacionalKg;

    @Column(name = "potencia_hp", nullable = true, precision = 8, scale = 2)
    private BigDecimal potenciaHp;

    @Column(name = "tipo_esteira_ou_pneu", nullable = true, length = 30)
    private String tipoEsteiraOuPneu;

    @Column(name = "capacidade_cacamba_m3", nullable = true, precision = 8, scale = 3)
    private BigDecimal capacidadeCacambaM3;

    @Column(name = "numero_serie", nullable = true, length = 100)
    private String numeroSerie;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected LinhaAmarelaEntity() { }

    public static LinhaAmarelaEntity criar(LinhaAmarela dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new LinhaAmarelaEntity();
        entity.veiculoId = Objects.requireNonNull(dados.getVeiculoId(), "veiculoId");
        entity.tipoMaquina = dados.getTipoMaquina();
        entity.horimetro = dados.getHorimetro();
        entity.pesoOperacionalKg = dados.getPesoOperacionalKg();
        entity.potenciaHp = dados.getPotenciaHp();
        entity.tipoEsteiraOuPneu = dados.getTipoEsteiraOuPneu();
        entity.capacidadeCacambaM3 = dados.getCapacidadeCacambaM3();
        entity.numeroSerie = dados.getNumeroSerie();
        return entity;
    }

    public void atualizar(LinhaAmarela dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(veiculoId, dados.getVeiculoId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.tipoMaquina = dados.getTipoMaquina();
        this.horimetro = dados.getHorimetro();
        this.pesoOperacionalKg = dados.getPesoOperacionalKg();
        this.potenciaHp = dados.getPotenciaHp();
        this.tipoEsteiraOuPneu = dados.getTipoEsteiraOuPneu();
        this.capacidadeCacambaM3 = dados.getCapacidadeCacambaM3();
        this.numeroSerie = dados.getNumeroSerie();
    }

    @Override
    public UUID getVeiculoId() { return veiculoId; }

    @Override
    public String getTipoMaquina() { return tipoMaquina; }

    @Override
    public Integer getHorimetro() { return horimetro; }

    @Override
    public Integer getPesoOperacionalKg() { return pesoOperacionalKg; }

    @Override
    public BigDecimal getPotenciaHp() { return potenciaHp; }

    @Override
    public String getTipoEsteiraOuPneu() { return tipoEsteiraOuPneu; }

    @Override
    public BigDecimal getCapacidadeCacambaM3() { return capacidadeCacambaM3; }

    @Override
    public String getNumeroSerie() { return numeroSerie; }

    public Long getLockVersion() { return lockVersion; }
}
