package repasse.phcauto.backend.domain.model.vendas;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de domínio de vendas.compras.
 * Sem dependência de persistência ou framework.
 */
public abstract class Compra {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getAnuncioId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getCompradorId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getVendedorId();

    /** Obrigatório no modelo inicial. */
    public abstract long getValorCentavos();

    /** Obrigatório no modelo inicial. */
    public abstract String getTituloVeiculoSnapshot();

    /** Obrigatório no modelo inicial. */
    public abstract StatusCompra getStatus();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getCriadoEm();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Instant getPagoEm();
}
