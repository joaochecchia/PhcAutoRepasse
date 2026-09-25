package repasse.phcauto.backend.infra.database.entity.vendas;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import repasse.phcauto.backend.domain.model.vendas.EventoGateway;

/** Mapeamento de persistência; use DTOs nas respostas HTTP. */
@Entity
@Table(name = "eventos_gateway", schema = "vendas")
@Access(AccessType.FIELD)
public class EventoGatewayEntity extends EventoGateway {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventos_gateway_seq")
    @SequenceGenerator(name = "eventos_gateway_seq", sequenceName = "vendas.eventos_gateway_seq", allocationSize = 50)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "provedor", nullable = false, length = 50)
    private String provedor;

    @Column(name = "evento_externo_id", nullable = false, length = 180)
    private String eventoExternoId;

    @Column(name = "pagamento_id", nullable = true)
    private UUID pagamentoId;

    @Column(name = "recebido_em", nullable = false, updatable = false)
    private Instant recebidoEm;

    @Column(name = "processado_em", nullable = true)
    private Instant processadoEm;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    protected EventoGatewayEntity() { }

    public static EventoGatewayEntity criar(EventoGateway dados) {
        Objects.requireNonNull(dados, "dados");
        var entity = new EventoGatewayEntity();
        entity.provedor = Objects.requireNonNull(dados.getProvedor(), "provedor");
        entity.eventoExternoId = Objects.requireNonNull(dados.getEventoExternoId(), "eventoExternoId");
        entity.pagamentoId = dados.getPagamentoId();
        entity.recebidoEm = Objects.requireNonNull(dados.getRecebidoEm(), "recebidoEm");
        entity.processadoEm = dados.getProcessadoEm();
        return entity;
    }

    public void atualizar(EventoGateway dados) {
        Objects.requireNonNull(dados, "dados");
        if (id == null || id.longValue() != dados.getId()) {
            throw new IllegalArgumentException("A identidade não pode ser alterada");
        }
        this.provedor = Objects.requireNonNull(dados.getProvedor(), "provedor");
        this.eventoExternoId = Objects.requireNonNull(dados.getEventoExternoId(), "eventoExternoId");
        this.pagamentoId = dados.getPagamentoId();
        this.processadoEm = dados.getProcessadoEm();
    }

    @Override
    public long getId() { return id == null ? 0 : id; }

    @Override
    public String getProvedor() { return provedor; }

    @Override
    public String getEventoExternoId() { return eventoExternoId; }

    @Override
    public UUID getPagamentoId() { return pagamentoId; }

    @Override
    public Instant getRecebidoEm() { return recebidoEm; }

    @Override
    public Instant getProcessadoEm() { return processadoEm; }

    public Long getLockVersion() { return lockVersion; }
}
