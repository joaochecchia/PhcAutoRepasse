package repasse.phcauto.backend.domain.model.catalogo;

import java.util.UUID;

/**
 * Contrato de domínio de catalogo.veiculo_caracteristicas.
 * Sem dependência de persistência ou framework.
 */
public abstract class VeiculoCaracteristica {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getVeiculoId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getCaracteristicaId();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getObservacao();
}
