package repasse.phcauto.backend.domain.model.catalogo;

import java.util.UUID;

/**
 * Contrato de domínio de catalogo.caminhoes.
 * Sem dependência de persistência ou framework.
 */
public abstract class Caminhao {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getVeiculoId();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getQuilometragem();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getConfiguracao();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getCarroceria();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getCambio();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getCombustivel();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getTracao();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getNumeroEixos();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getCapacidadeCargaKg();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getPesoBrutoTotalKg();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getImplemento();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getFinalPlaca();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Boolean getIpvaPago();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Boolean getLicenciado();
}
