package repasse.phcauto.backend.domain.model.catalogo;

import java.util.UUID;

/**
 * Contrato de domínio de catalogo.caracteristicas.
 * Sem dependência de persistência ou framework.
 */
public abstract class Caracteristica {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract TipoVeiculo getTipoVeiculo();

    /** Obrigatório no modelo inicial. */
    public abstract String getNome();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getGrupo();
}
