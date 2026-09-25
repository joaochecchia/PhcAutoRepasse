package repasse.phcauto.backend.infra.controller.dto.identidade;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;

/** Campos obrigatórios comuns ao cadastro local e social de pessoa jurídica. */
@Getter
@Setter
@NoArgsConstructor
public class DadosCadastroPessoaJuridicaRequest {

    @NotBlank(message = "nome da empresa é obrigatório")
    @Size(max = 200, message = "nome da empresa deve ter no máximo 200 caracteres")
    private String nomeEmpresa;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos")
    @CNPJ(message = "CNPJ inválido")
    private String cnpj;

    @NotBlank(message = "razão social é obrigatória")
    @Size(max = 200, message = "razão social deve ter no máximo 200 caracteres")
    private String razaoSocial;

    @NotBlank(message = "número de contato é obrigatório")
    @Pattern(regexp = "\\+?[1-9]\\d{9,14}", message = "número de contato inválido")
    private String numeroContato;

    @NotBlank(message = "email de contato é obrigatório")
    @Email(message = "email de contato inválido")
    @Size(max = 254, message = "email de contato deve ter no máximo 254 caracteres")
    private String emailContato;

    @Valid
    @NotNull(message = "endereço é obrigatório")
    private EnderecoCadastroRequest endereco;
}
