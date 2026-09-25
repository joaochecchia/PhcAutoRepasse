package repasse.phcauto.backend.domain.model.identidade;

import java.util.UUID;

/** Dados persistíveis; o preenchimento por etapa pertence ao caso de uso. */
public abstract class DadosCompraPj {
    public abstract UUID getUsuarioId();
    public abstract String getInscricaoEstadual();
    public abstract String getRegimeTributario();
}
