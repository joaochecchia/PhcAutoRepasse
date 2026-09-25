package repasse.phcauto.backend.domain.model.identidade;

import java.util.UUID;

/** Dados persistíveis; o preenchimento por etapa pertence ao caso de uso. */
public abstract class IdentidadeExterna {
    public abstract UUID getId();
    public abstract UUID getUsuarioId();
    public abstract ProvedorAutenticacao getProvedor();
    public abstract String getIdentificadorExterno();
}
