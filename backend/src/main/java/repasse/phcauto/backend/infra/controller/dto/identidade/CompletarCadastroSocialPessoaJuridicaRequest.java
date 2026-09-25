package repasse.phcauto.backend.infra.controller.dto.identidade;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Completa os dados de uma PJ depois que Google/Facebook já autenticou a conta. */
@Getter
@Setter
@NoArgsConstructor
public class CompletarCadastroSocialPessoaJuridicaRequest {

    @Valid
    @NotNull(message = "dados cadastrais são obrigatórios")
    private DadosCadastroPessoaJuridicaRequest dados;
}
