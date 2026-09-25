package repasse.phcauto.backend.domain.model.identidade;

import java.util.UUID;

/** Dados persistíveis; o preenchimento por etapa pertence ao caso de uso. */
public abstract class DadosCompraPf {
    public abstract UUID getUsuarioId();
    public abstract String getRg();
    public abstract String getNomePai();
    public abstract String getNomeMae();
    public abstract String getNaturalidade();
    public abstract String getGenero();
}
