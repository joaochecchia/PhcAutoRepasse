package repasse.phcauto.backend.domain.model.catalogo;

import java.util.UUID;

/**
 * Contrato de domínio de catalogo.motos.
 * Sem dependência de persistência ou framework.
 */
public abstract class Moto {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getVeiculoId();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getQuilometragem();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getCilindradas();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getCategoria();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getPartida();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getRefrigeracao();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getCambio();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getCombustivel();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getFinalPlaca();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Boolean getIpvaPago();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Boolean getLicenciado();
}
