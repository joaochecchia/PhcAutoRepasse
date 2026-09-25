package repasse.phcauto.backend.usuarios;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import repasse.phcauto.backend.domain.model.identidade.TipoPessoa;

public record CriarUsuarioRequest(
        @NotNull TipoPessoa tipoPessoa,
        @NotBlank @Size(max = 160) String nome,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Pattern(regexp = "\\+?[1-9][0-9]{9,14}") String telefone,
        @NotBlank @Size(min = 8, max = 128)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha,
        String cpf, LocalDate dataNascimento, String cnpj, String razaoSocial,
        @Valid @NotNull EnderecoRequest endereco) {
    @Override public String toString() { return "CriarUsuarioRequest[conteudo protegido]"; }
}
