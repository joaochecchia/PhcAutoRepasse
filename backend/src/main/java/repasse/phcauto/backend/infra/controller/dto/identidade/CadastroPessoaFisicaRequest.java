package repasse.phcauto.backend.infra.controller.dto.identidade;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Requisição de cadastro local de pessoa física. */
@Getter
@Setter
@NoArgsConstructor
public class CadastroPessoaFisicaRequest {

    @Valid
    @NotNull(message = "dados cadastrais são obrigatórios")
    private DadosCadastroPessoaFisicaRequest dados;

    @NotBlank(message = "senha é obrigatória no cadastro local")
    @Size(min = 8, max = 128, message = "senha deve ter entre 8 e 128 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;
}
