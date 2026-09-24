package repasse.phcauto.backend.domain.model.assinaturas;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato de domínio de assinaturas.planos.
 * Sem dependência de persistência ou framework.
 */
public abstract class Plano {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract String getNome();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Long getValorCentavos();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getPeriodoMeses();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getLimiteAnuncios();

    /** Obrigatório no modelo inicial. */
    public abstract boolean getAtivo();

    /** Obrigatório no modelo inicial. */
    public abstract Instant getCriadoEm();
}
