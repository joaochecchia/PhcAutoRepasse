package repasse.phcauto.backend.domain.model.catalogo;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Contrato de domínio de catalogo.linha_amarela.
 * Sem dependência de persistência ou framework.
 */
public abstract class LinhaAmarela {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getVeiculoId();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getTipoMaquina();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getHorimetro();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getPesoOperacionalKg();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract BigDecimal getPotenciaHp();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getTipoEsteiraOuPneu();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract BigDecimal getCapacidadeCacambaM3();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getNumeroSerie();
}
