package repasse.phcauto.backend.domain.model.identidade;

import java.util.UUID;

/** Dados persistíveis; o preenchimento por etapa pertence ao caso de uso. */
public abstract class EnderecoUsuario {
    public abstract UUID getUsuarioId();
    public abstract String getCep();
    public abstract String getCidade();
    public abstract String getBairro();
    public abstract String getRua();
    public abstract String getNumero();
    public abstract String getComplemento();
    public abstract String getUf();
}
