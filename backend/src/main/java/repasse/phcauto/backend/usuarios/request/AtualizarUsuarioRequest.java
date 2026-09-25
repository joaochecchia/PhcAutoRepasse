package repasse.phcauto.backend.usuarios.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/** Campos ausentes ou nulos permanecem inalterados; texto vazio limpa campos opcionais. */
public record AtualizarUsuarioRequest(
        String nome,
        String email,
        String telefone,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha,
        String cpf,
        LocalDate dataNascimento,
        String cnpj,
        String razaoSocial,
        EnderecoPatchRequest endereco) {

    @Override public String toString() { return "AtualizarUsuarioRequest[conteudo protegido]"; }
}
