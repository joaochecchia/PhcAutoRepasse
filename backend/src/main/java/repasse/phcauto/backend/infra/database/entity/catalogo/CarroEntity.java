package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import repasse.phcauto.backend.domain.model.catalogo.Carro;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "carros", schema = "catalogo")
@Access(AccessType.FIELD)
public class CarroEntity extends Carro {

    @Id
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private UUID veiculoId;

    @Column(name = "quilometragem", nullable = true)
    private Integer quilometragem;

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

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "numero_portas", nullable = true)
    private Integer numeroPortas;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "numero_lugares", nullable = true)
    private Integer numeroLugares;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "final_placa", nullable = true, length = 1, columnDefinition = "char(1)")
    private String finalPlaca;

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

    protected CarroEntity() { }

    public static CarroEntity criar(Carro dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new CarroEntity();
        entity.veiculoId = Objects.requireNonNull(dados.getVeiculoId(), "veiculoId");
        entity.quilometragem = dados.getQuilometragem();
        entity.carroceria = dados.getCarroceria();
        entity.cambio = dados.getCambio();
        entity.combustivel = dados.getCombustivel();
        entity.tracao = dados.getTracao();
        entity.motorizacao = dados.getMotorizacao();
        entity.numeroPortas = dados.getNumeroPortas();
        entity.numeroLugares = dados.getNumeroLugares();
        entity.finalPlaca = dados.getFinalPlaca();
        entity.unicoDono = dados.getUnicoDono();
        entity.ipvaPago = dados.getIpvaPago();
        entity.licenciado = dados.getLicenciado();
        entity.blindado = dados.getBlindado();
        return entity;
    }

    public void atualizar(Carro dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(veiculoId, dados.getVeiculoId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.quilometragem = dados.getQuilometragem();
        this.carroceria = dados.getCarroceria();
        this.cambio = dados.getCambio();
        this.combustivel = dados.getCombustivel();
        this.tracao = dados.getTracao();
        this.motorizacao = dados.getMotorizacao();
        this.numeroPortas = dados.getNumeroPortas();
        this.numeroLugares = dados.getNumeroLugares();
        this.finalPlaca = dados.getFinalPlaca();
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
    public Integer getNumeroPortas() { return numeroPortas; }

    @Override
    public Integer getNumeroLugares() { return numeroLugares; }

    @Override
    public String getFinalPlaca() { return finalPlaca; }

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
