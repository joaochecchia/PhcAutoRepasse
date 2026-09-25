package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.catalogo.Caminhao;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "caminhoes", schema = "catalogo")
@Access(AccessType.FIELD)
public class CaminhaoEntity extends Caminhao {

    @Id
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private UUID veiculoId;

    @Column(name = "quilometragem", nullable = true)
    private Integer quilometragem;

    @Column(name = "configuracao", nullable = true, length = 80)
    private String configuracao;

    @Column(name = "carroceria", nullable = true, length = 80)
    private String carroceria;

    @Column(name = "cambio", nullable = true, length = 60)
    private String cambio;

    @Column(name = "combustivel", nullable = true, length = 60)
    private String combustivel;

    @Column(name = "tracao", nullable = true, length = 40)
    private String tracao;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "numero_eixos", nullable = true)
    private Integer numeroEixos;

    @Column(name = "capacidade_carga_kg", nullable = true)
    private Integer capacidadeCargaKg;

    @Column(name = "peso_bruto_total_kg", nullable = true)
    private Integer pesoBrutoTotalKg;

    @Column(name = "implemento", nullable = true, length = 100)
    private String implemento;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "final_placa", nullable = true, length = 1, columnDefinition = "char(1)")
    private String finalPlaca;

    @Column(name = "ipva_pago", nullable = true)
    private Boolean ipvaPago;

    @Column(name = "licenciado", nullable = true)
    private Boolean licenciado;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected CaminhaoEntity() { }

    public static CaminhaoEntity criar(Caminhao dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new CaminhaoEntity();
        entity.veiculoId = Objects.requireNonNull(dados.getVeiculoId(), "veiculoId");
        entity.quilometragem = dados.getQuilometragem();
        entity.configuracao = dados.getConfiguracao();
        entity.carroceria = dados.getCarroceria();
        entity.cambio = dados.getCambio();
        entity.combustivel = dados.getCombustivel();
        entity.tracao = dados.getTracao();
        entity.numeroEixos = dados.getNumeroEixos();
        entity.capacidadeCargaKg = dados.getCapacidadeCargaKg();
        entity.pesoBrutoTotalKg = dados.getPesoBrutoTotalKg();
        entity.implemento = dados.getImplemento();
        entity.finalPlaca = dados.getFinalPlaca();
        entity.ipvaPago = dados.getIpvaPago();
        entity.licenciado = dados.getLicenciado();
        return entity;
    }

    public void atualizar(Caminhao dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(veiculoId, dados.getVeiculoId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.quilometragem = dados.getQuilometragem();
        this.configuracao = dados.getConfiguracao();
        this.carroceria = dados.getCarroceria();
        this.cambio = dados.getCambio();
        this.combustivel = dados.getCombustivel();
        this.tracao = dados.getTracao();
        this.numeroEixos = dados.getNumeroEixos();
        this.capacidadeCargaKg = dados.getCapacidadeCargaKg();
        this.pesoBrutoTotalKg = dados.getPesoBrutoTotalKg();
        this.implemento = dados.getImplemento();
        this.finalPlaca = dados.getFinalPlaca();
        this.ipvaPago = dados.getIpvaPago();
        this.licenciado = dados.getLicenciado();
    }

    @Override
    public UUID getVeiculoId() { return veiculoId; }

    @Override
    public Integer getQuilometragem() { return quilometragem; }

    @Override
    public String getConfiguracao() { return configuracao; }

    @Override
    public String getCarroceria() { return carroceria; }

    @Override
    public String getCambio() { return cambio; }

    @Override
    public String getCombustivel() { return combustivel; }

    @Override
    public String getTracao() { return tracao; }

    @Override
    public Integer getNumeroEixos() { return numeroEixos; }

    @Override
    public Integer getCapacidadeCargaKg() { return capacidadeCargaKg; }

    @Override
    public Integer getPesoBrutoTotalKg() { return pesoBrutoTotalKg; }

    @Override
    public String getImplemento() { return implemento; }

    @Override
    public String getFinalPlaca() { return finalPlaca; }

    @Override
    public Boolean getIpvaPago() { return ipvaPago; }

    @Override
    public Boolean getLicenciado() { return licenciado; }

    public Long getLockVersion() { return lockVersion; }
}
