package repasse.phcauto.backend.domain.model.vendas;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de domínio de vendas.pagamentos.
 * Sem dependência de persistência ou framework.
 */
public abstract class Pagamento {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getCompraId();

    /** Obrigatório no modelo inicial. */
    public abstract String getProvedor();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getReferenciaExterna();

    /** Obrigatório no modelo inicial. */
    public abstract MetodoPagamento getMetodo();

    /** Obrigatório no modelo inicial. */
    public abstract StatusPagamento getStatus();

    /** Obrigatório no modelo inicial. */
    public abstract long getValorCentavos();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Instant getVenceEm();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Instant getConfirmadoEm();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getCriadoEm();
}
