package repasse.phcauto.backend.infra.controller.dto.identidade;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Dados públicos do cadastro PF. Nunca transporta senha ou hash de senha. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PessoaFisicaResponse {
    private UUID id;
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private String email;
    private String numeroCelular;
    private EnderecoResponse endereco;
}
