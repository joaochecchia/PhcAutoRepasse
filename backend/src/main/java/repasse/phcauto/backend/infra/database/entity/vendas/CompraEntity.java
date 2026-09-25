package repasse.phcauto.backend.infra.database.entity.vendas;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.vendas.Compra;
import repasse.phcauto.backend.domain.model.vendas.StatusCompra;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "compras", schema = "vendas")
@Access(AccessType.FIELD)
public class CompraEntity extends Compra {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "anuncio_id", nullable = false)
    private UUID anuncioId;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(name = "vendedor_id", nullable = false)
    private UUID vendedorId;

    @Column(name = "valor_centavos", nullable = false)
    private long valorCentavos;

    @Column(name = "titulo_veiculo_snapshot", nullable = false, length = 250)
    private String tituloVeiculoSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private StatusCompra status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "pago_em", nullable = true)
    private Instant pagoEm;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected CompraEntity() { }

    public static CompraEntity criar(Compra dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new CompraEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.anuncioId = Objects.requireNonNull(dados.getAnuncioId(), "anuncioId");
        entity.compradorId = Objects.requireNonNull(dados.getCompradorId(), "compradorId");
        entity.vendedorId = Objects.requireNonNull(dados.getVendedorId(), "vendedorId");
        entity.valorCentavos = dados.getValorCentavos();
        entity.tituloVeiculoSnapshot = Objects.requireNonNull(dados.getTituloVeiculoSnapshot(), "tituloVeiculoSnapshot");
        entity.status = Objects.requireNonNull(dados.getStatus(), "status");
        entity.criadoEm = Objects.requireNonNull(dados.getCriadoEm(), "criadoEm");
        entity.pagoEm = dados.getPagoEm();
        return entity;
    }

    public void atualizar(Compra dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.anuncioId = Objects.requireNonNull(dados.getAnuncioId(), "anuncioId");
        this.compradorId = Objects.requireNonNull(dados.getCompradorId(), "compradorId");
        this.vendedorId = Objects.requireNonNull(dados.getVendedorId(), "vendedorId");
        this.valorCentavos = dados.getValorCentavos();
        this.tituloVeiculoSnapshot = Objects.requireNonNull(dados.getTituloVeiculoSnapshot(), "tituloVeiculoSnapshot");
        this.status = Objects.requireNonNull(dados.getStatus(), "status");
        this.pagoEm = dados.getPagoEm();
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public UUID getAnuncioId() { return anuncioId; }

    @Override
    public UUID getCompradorId() { return compradorId; }

    @Override
    public UUID getVendedorId() { return vendedorId; }

    @Override
    public long getValorCentavos() { return valorCentavos; }

    @Override
    public String getTituloVeiculoSnapshot() { return tituloVeiculoSnapshot; }

    @Override
    public StatusCompra getStatus() { return status; }

    @Override
    public Instant getCriadoEm() { return criadoEm; }

    @Override
    public Instant getPagoEm() { return pagoEm; }

    public Long getLockVersion() { return lockVersion; }
}
