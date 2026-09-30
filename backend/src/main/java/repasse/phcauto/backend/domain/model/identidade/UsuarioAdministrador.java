package repasse.phcauto.backend.domain.model.identidade;

import java.time.LocalDate;
import java.util.UUID;

/** Dados obrigatórios do perfil administrativo; o usuário continua sendo a raiz do agregado. */
public abstract class UsuarioAdministrador {
    public abstract UUID getUsuarioId();
    public abstract TipoPessoa getTipoPessoa();
    public abstract String getCpf();
    public abstract LocalDate getDataNascimento();
    public abstract String getCnpj();
    public abstract String getRazaoSocial();
}
