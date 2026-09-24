package repasse.phcauto.backend.domain.model.vendas;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de domínio de vendas.eventos_gateway.
 * Sem dependência de persistência ou framework.
 */
public abstract class EventoGateway {

    /** Obrigatório no modelo inicial. */
    public abstract long getId();

    /** Obrigatório no modelo inicial. */
    public abstract String getProvedor();

    /** Obrigatório no modelo inicial. */
    public abstract String getEventoExternoId();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract UUID getPagamentoId();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getRecebidoEm();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Instant getProcessadoEm();
}
