package repasse.phcauto.backend.domain.model.catalogo;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Contrato de domínio de catalogo.barcos.
 * Sem dependência de persistência ou framework.
 */
public abstract class Barco {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getVeiculoId();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract BigDecimal getTamanhoPes();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getEstilo();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getMaterialCasco();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getCapacidadePessoas();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getNumeroCabines();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract Integer getHorasUso();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getRegistroMaritimo();
}
