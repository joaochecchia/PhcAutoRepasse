package repasse.phcauto.backend.infra.database.entity.catalogo;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.catalogo.Moto;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "motos", schema = "catalogo")
@Access(AccessType.FIELD)
public class MotoEntity extends Moto {

    @Id
    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private UUID veiculoId;

    @Column(name = "quilometragem", nullable = true)
    private Integer quilometragem;

    @Column(name = "cilindradas", nullable = true)
    private Integer cilindradas;

    @Column(name = "categoria", nullable = true, length = 60)
    private String categoria;

    @Column(name = "partida", nullable = true, length = 40)
    private String partida;

    @Column(name = "refrigeracao", nullable = true, length = 40)
    private String refrigeracao;

    @Column(name = "cambio", nullable = true, length = 60)
    private String cambio;

    @Column(name = "combustivel", nullable = true, length = 60)
    private String combustivel;

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

    protected MotoEntity() { }

    public static MotoEntity criar(Moto dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new MotoEntity();
        entity.veiculoId = Objects.requireNonNull(dados.getVeiculoId(), "veiculoId");
        entity.quilometragem = dados.getQuilometragem();
        entity.cilindradas = dados.getCilindradas();
        entity.categoria = dados.getCategoria();
        entity.partida = dados.getPartida();
        entity.refrigeracao = dados.getRefrigeracao();
        entity.cambio = dados.getCambio();
        entity.combustivel = dados.getCombustivel();
        entity.finalPlaca = dados.getFinalPlaca();
        entity.ipvaPago = dados.getIpvaPago();
        entity.licenciado = dados.getLicenciado();
        return entity;
    }

    public void atualizar(Moto dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(veiculoId, dados.getVeiculoId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.quilometragem = dados.getQuilometragem();
        this.cilindradas = dados.getCilindradas();
        this.categoria = dados.getCategoria();
        this.partida = dados.getPartida();
        this.refrigeracao = dados.getRefrigeracao();
        this.cambio = dados.getCambio();
        this.combustivel = dados.getCombustivel();
        this.finalPlaca = dados.getFinalPlaca();
        this.ipvaPago = dados.getIpvaPago();
        this.licenciado = dados.getLicenciado();
    }

    @Override
    public UUID getVeiculoId() { return veiculoId; }

    @Override
    public Integer getQuilometragem() { return quilometragem; }

    @Override
    public Integer getCilindradas() { return cilindradas; }

    @Override
    public String getCategoria() { return categoria; }

    @Override
    public String getPartida() { return partida; }

    @Override
    public String getRefrigeracao() { return refrigeracao; }

    @Override
    public String getCambio() { return cambio; }

    @Override
    public String getCombustivel() { return combustivel; }

    @Override
    public String getFinalPlaca() { return finalPlaca; }

    @Override
    public Boolean getIpvaPago() { return ipvaPago; }

    @Override
    public Boolean getLicenciado() { return licenciado; }

    public Long getLockVersion() { return lockVersion; }
}
