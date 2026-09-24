package repasse.phcauto.backend.domain.model.catalogo;

import java.util.UUID;

/**
 * Contrato de domínio de catalogo.fotos.
 * Sem dependência de persistência ou framework.
 */
public abstract class Foto {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getId();

    /** Obrigatório no modelo inicial. */
    public abstract UUID getAnuncioId();

    /** Obrigatório no modelo inicial. */
    public abstract String getChaveArquivo();

    /** Obrigatório no modelo inicial. */
    public abstract int getPosicao();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getTextoAlternativo();
}
