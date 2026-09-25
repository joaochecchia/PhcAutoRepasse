package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.catalogo.Barco;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "barcos", schema = "catalogo")
@Access(AccessType.FIELD)
public class BarcoEntity extends Barco {

    @Id
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private UUID veiculoId;

    @Column(name = "tamanho_pes", nullable = true, precision = 7, scale = 2)
    private BigDecimal tamanhoPes;

    @Column(name = "estilo", nullable = true, length = 80)
    private String estilo;

    @Column(name = "material_casco", nullable = true, length = 80)
    private String materialCasco;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "capacidade_pessoas", nullable = true)
    private Integer capacidadePessoas;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "numero_cabines", nullable = true)
    private Integer numeroCabines;

    @Column(name = "horas_uso", nullable = true)
    private Integer horasUso;

    @Column(name = "registro_maritimo", nullable = true, length = 80)
    private String registroMaritimo;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected BarcoEntity() { }

    public static BarcoEntity criar(Barco dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new BarcoEntity();
        entity.veiculoId = Objects.requireNonNull(dados.getVeiculoId(), "veiculoId");
        entity.tamanhoPes = dados.getTamanhoPes();
        entity.estilo = dados.getEstilo();
        entity.materialCasco = dados.getMaterialCasco();
        entity.capacidadePessoas = dados.getCapacidadePessoas();
        entity.numeroCabines = dados.getNumeroCabines();
        entity.horasUso = dados.getHorasUso();
        entity.registroMaritimo = dados.getRegistroMaritimo();
        return entity;
    }

    public void atualizar(Barco dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(veiculoId, dados.getVeiculoId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.tamanhoPes = dados.getTamanhoPes();
        this.estilo = dados.getEstilo();
        this.materialCasco = dados.getMaterialCasco();
        this.capacidadePessoas = dados.getCapacidadePessoas();
        this.numeroCabines = dados.getNumeroCabines();
        this.horasUso = dados.getHorasUso();
        this.registroMaritimo = dados.getRegistroMaritimo();
    }

    @Override
    public UUID getVeiculoId() { return veiculoId; }

    @Override
    public BigDecimal getTamanhoPes() { return tamanhoPes; }

    @Override
    public String getEstilo() { return estilo; }

    @Override
    public String getMaterialCasco() { return materialCasco; }

    @Override
    public Integer getCapacidadePessoas() { return capacidadePessoas; }

    @Override
    public Integer getNumeroCabines() { return numeroCabines; }

    @Override
    public Integer getHorasUso() { return horasUso; }

    @Override
    public String getRegistroMaritimo() { return registroMaritimo; }

    public Long getLockVersion() { return lockVersion; }
}
