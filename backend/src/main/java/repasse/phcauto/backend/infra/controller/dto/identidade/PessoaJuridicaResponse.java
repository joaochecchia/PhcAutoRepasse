package repasse.phcauto.backend.infra.controller.dto.identidade;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Dados públicos do cadastro PJ. Nunca transporta senha ou hash de senha. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PessoaJuridicaResponse {
    private UUID id;
    private String nomeEmpresa;
    private String cnpj;
    private String razaoSocial;
    private String numeroContato;
    private String emailContato;
    private EnderecoResponse endereco;
}
