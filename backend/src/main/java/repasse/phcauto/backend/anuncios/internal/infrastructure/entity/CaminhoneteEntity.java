package repasse.phcauto.backend.anuncios.internal.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.catalogo.Caminhonete;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "caminhonetes", schema = "catalogo")
@Access(AccessType.FIELD)
public class CaminhoneteEntity extends Caminhonete {

    @Id
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private UUID veiculoId;

    @Column(name = "quilometragem", nullable = true)
    private Integer quilometragem;

    @Column(name = "tipo_cabine", nullable = true, length = 50)
    private String tipoCabine;

    @Column(name = "carroceria", nullable = true, length = 60)
    private String carroceria;

    @Column(name = "cambio", nullable = true, length = 60)
    private String cambio;

    @Column(name = "combustivel", nullable = true, length = 60)
    private String combustivel;

    @Column(name = "tracao", nullable = true, length = 40)
    private String tracao;

    @Column(name = "motorizacao", nullable = true, length = 100)
    private String motorizacao;

    @Column(name = "tipo_direcao", nullable = true, length = 60)
    private String tipoDirecao;

    @Column(name = "cilindrada_litros", nullable = true, precision = 4, scale = 1)
    private BigDecimal cilindradaLitros;

    @Column(name = "capacidade_carga_kg", nullable = true)
    private Integer capacidadeCargaKg;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "numero_portas", nullable = true)
    private Integer numeroPortas;

    @Column(name = "placa", nullable = true, length = 7)
    private String placa;

    @Column(name = "exibir_placa_completa", nullable = false)
    private Boolean exibirPlacaCompleta;

    @Column(name = "unico_dono", nullable = true)
    private Boolean unicoDono;

    @Column(name = "ipva_pago", nullable = true)
    private Boolean ipvaPago;

    @Column(name = "licenciado", nullable = true)
    private Boolean licenciado;

    @Column(name = "blindado", nullable = true)
    private Boolean blindado;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected CaminhoneteEntity() { }

    public static CaminhoneteEntity criar(Caminhonete dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new CaminhoneteEntity();
        entity.veiculoId = Objects.requireNonNull(dados.getVeiculoId(), "veiculoId");
        entity.quilometragem = dados.getQuilometragem();
        entity.tipoCabine = dados.getTipoCabine();
        entity.carroceria = dados.getCarroceria();
        entity.cambio = dados.getCambio();
        entity.combustivel = dados.getCombustivel();
        entity.tracao = dados.getTracao();
        entity.motorizacao = dados.getMotorizacao();
        entity.tipoDirecao = dados.getTipoDirecao();
        entity.cilindradaLitros = dados.getCilindradaLitros();
        entity.capacidadeCargaKg = dados.getCapacidadeCargaKg();
        entity.numeroPortas = dados.getNumeroPortas();
        entity.placa = dados.getPlaca();
        entity.exibirPlacaCompleta = dados.getExibirPlacaCompleta();
        entity.unicoDono = dados.getUnicoDono();
        entity.ipvaPago = dados.getIpvaPago();
        entity.licenciado = dados.getLicenciado();
        entity.blindado = dados.getBlindado();
        return entity;
    }

    public void atualizar(Caminhonete dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(veiculoId, dados.getVeiculoId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.quilometragem = dados.getQuilometragem();
        this.tipoCabine = dados.getTipoCabine();
        this.carroceria = dados.getCarroceria();
        this.cambio = dados.getCambio();
        this.combustivel = dados.getCombustivel();
        this.tracao = dados.getTracao();
        this.motorizacao = dados.getMotorizacao();
        this.tipoDirecao = dados.getTipoDirecao();
        this.cilindradaLitros = dados.getCilindradaLitros();
        this.capacidadeCargaKg = dados.getCapacidadeCargaKg();
        this.numeroPortas = dados.getNumeroPortas();
        this.placa = dados.getPlaca();
        this.exibirPlacaCompleta = dados.getExibirPlacaCompleta();
        this.unicoDono = dados.getUnicoDono();
        this.ipvaPago = dados.getIpvaPago();
        this.licenciado = dados.getLicenciado();
        this.blindado = dados.getBlindado();
    }

    @Override
    public UUID getVeiculoId() { return veiculoId; }

    @Override
    public Integer getQuilometragem() { return quilometragem; }

    @Override
    public String getTipoCabine() { return tipoCabine; }

    @Override
    public String getCarroceria() { return carroceria; }

    @Override
    public String getCambio() { return cambio; }

    @Override
    public String getCombustivel() { return combustivel; }

    @Override
    public String getTracao() { return tracao; }

    @Override
    public String getMotorizacao() { return motorizacao; }

    @Override
    public String getTipoDirecao() { return tipoDirecao; }

    @Override
    public BigDecimal getCilindradaLitros() { return cilindradaLitros; }

    @Override
    public Integer getCapacidadeCargaKg() { return capacidadeCargaKg; }

    @Override
    public Integer getNumeroPortas() { return numeroPortas; }

    @Override
    public String getPlaca() { return placa; }

    @Override
    public Boolean getExibirPlacaCompleta() { return exibirPlacaCompleta; }

    @Override
    public Boolean getUnicoDono() { return unicoDono; }

    @Override
    public Boolean getIpvaPago() { return ipvaPago; }

    @Override
    public Boolean getLicenciado() { return licenciado; }

    @Override
    public Boolean getBlindado() { return blindado; }

    public Long getLockVersion() { return lockVersion; }
}
