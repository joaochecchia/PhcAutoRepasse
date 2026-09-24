package repasse.phcauto.backend.domain.model.assinaturas;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de domínio de assinaturas.assinaturas.
 * Sem dependência de persistência ou framework.
 */
public abstract class Assinatura {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getUsuarioId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getPlanoId();

    /** Obrigatório no modelo inicial. */
    public abstract long getValorContratadoCentavos();

    /** Obrigatório no modelo inicial. */
    public abstract StatusAssinatura getStatus();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Instant getInicioEm();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Instant getFimEm();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getCriadoEm();
}
