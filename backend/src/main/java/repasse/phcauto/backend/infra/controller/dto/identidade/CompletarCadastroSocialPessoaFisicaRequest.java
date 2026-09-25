package repasse.phcauto.backend.infra.controller.dto.identidade;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Completa os dados de uma PF depois que Google/Facebook já autenticou a conta. */
@Getter
@Setter
@NoArgsConstructor
public class CompletarCadastroSocialPessoaFisicaRequest {

    @Valid
    @NotNull(message = "dados cadastrais são obrigatórios")
    private DadosCadastroPessoaFisicaRequest dados;
}
