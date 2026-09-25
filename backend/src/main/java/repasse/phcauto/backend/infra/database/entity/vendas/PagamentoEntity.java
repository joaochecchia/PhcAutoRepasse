package repasse.phcauto.backend.infra.database.entity.vendas;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.vendas.MetodoPagamento;
import repasse.phcauto.backend.domain.model.vendas.Pagamento;
import repasse.phcauto.backend.domain.model.vendas.StatusPagamento;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "pagamentos", schema = "vendas")
@Access(AccessType.FIELD)
public class PagamentoEntity extends Pagamento {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "compra_id", nullable = false)
    private UUID compraId;

    @Column(name = "provedor", nullable = false, length = 50)
    private String provedor;

    @Column(name = "referencia_externa", nullable = true, length = 180)
    private String referenciaExterna;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", nullable = false, length = 32)
    private MetodoPagamento metodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private StatusPagamento status;

    @Column(name = "valor_centavos", nullable = false)
    private long valorCentavos;

    @Column(name = "vence_em", nullable = true)
    private Instant venceEm;

    @Column(name = "confirmado_em", nullable = true)
    private Instant confirmadoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected PagamentoEntity() { }

    public static PagamentoEntity criar(Pagamento dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new PagamentoEntity();
        entity.id = dados.getId() == null ? UUID.randomUUID() : dados.getId();
        entity.compraId = Objects.requireNonNull(dados.getCompraId(), "compraId");
        entity.provedor = Objects.requireNonNull(dados.getProvedor(), "provedor");
        entity.referenciaExterna = dados.getReferenciaExterna();
        entity.metodo = Objects.requireNonNull(dados.getMetodo(), "metodo");
        entity.status = Objects.requireNonNull(dados.getStatus(), "status");
        entity.valorCentavos = dados.getValorCentavos();
        entity.venceEm = dados.getVenceEm();
        entity.confirmadoEm = dados.getConfirmadoEm();
        entity.criadoEm = Objects.requireNonNull(dados.getCriadoEm(), "criadoEm");
        return entity;
    }

    public void atualizar(Pagamento dados) {
        Objects.requireNonNull(dados, "dados");
        if (!Objects.equals(id, dados.getId())) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.compraId = Objects.requireNonNull(dados.getCompraId(), "compraId");
        this.provedor = Objects.requireNonNull(dados.getProvedor(), "provedor");
        this.referenciaExterna = dados.getReferenciaExterna();
        this.metodo = Objects.requireNonNull(dados.getMetodo(), "metodo");
        this.status = Objects.requireNonNull(dados.getStatus(), "status");
        this.valorCentavos = dados.getValorCentavos();
        this.venceEm = dados.getVenceEm();
        this.confirmadoEm = dados.getConfirmadoEm();
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public UUID getCompraId() { return compraId; }

    @Override
    public String getProvedor() { return provedor; }

    @Override
    public String getReferenciaExterna() { return referenciaExterna; }

    @Override
    public MetodoPagamento getMetodo() { return metodo; }

    @Override
    public StatusPagamento getStatus() { return status; }

    @Override
    public long getValorCentavos() { return valorCentavos; }

    @Override
    public Instant getVenceEm() { return venceEm; }

    @Override
    public Instant getConfirmadoEm() { return confirmadoEm; }

    @Override
    public Instant getCriadoEm() { return criadoEm; }

    public Long getLockVersion() { return lockVersion; }
}
