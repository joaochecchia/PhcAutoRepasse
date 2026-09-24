package repasse.phcauto.backend.domain.model.identidade;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrato de domínio de identidade.usuarios_pf.
 * Sem dependência de persistência ou framework.
 */
public abstract class UsuarioPf {

    /** Obrigatório no modelo inicial. */
    public abstract UUID getUsuarioId();

    /** Obrigatório no modelo inicial. */
    public abstract String getCpf();

    /** Opcional no modelo inicial; pode retornar null. */
    public abstract LocalDate getDataNascimento();
}
