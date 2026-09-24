package repasse.phcauto.backend.domain.model.identidade;

import java.util.UUID;

/**
 * Contrato de domínio de identidade.usuarios_pj.
 * Sem dependência de persistência ou framework.
 */
public abstract class UsuarioPj {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getUsuarioId();

    /** Obrigatório no modelo inicial. */
    public abstract String getCnpj();

    /** Obrigatório no modelo inicial. */
    public abstract String getRazaoSocial();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract String getNomeFantasia();
}
