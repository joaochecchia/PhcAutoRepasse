package repasse.phcauto.backend.domain.model.catalogo;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Contrato de domínio de catalogo.motores_barco.
 * Sem dependência de persistência ou framework.
 */
public abstract class MotorBarco {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getBarcoId();

    /** Obrigatório no modelo inicial. */
    public abstract int getPosicao();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getFabricante();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getModelo();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract BigDecimal getPotenciaHp();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getAno();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getHorasUso();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getHorasDesdeRevisao();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getCombustivel();
}
