package repasse.phcauto.backend.infra.controller.dto.identidade;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

/** Campos obrigatórios comuns ao cadastro local e social de pessoa física. */
@Getter
@Setter
@NoArgsConstructor
public class DadosCadastroPessoaFisicaRequest {

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    @CPF(message = "CPF inválido")
    private String cpf;

    @NotBlank(message = "nome é obrigatório")
    @Size(max = 160, message = "nome deve ter no máximo 160 caracteres")
    private String nome;

    @NotNull(message = "data de nascimento é obrigatória")
    @Past(message = "data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    @Size(max = 254, message = "email deve ter no máximo 254 caracteres")
    private String email;

    @NotBlank(message = "número de celular é obrigatório")
    @Pattern(regexp = "\\+?[1-9]\\d{9,14}", message = "número de celular inválido")
    private String numeroCelular;

    @Valid
    @NotNull(message = "endereço é obrigatório")
    private EnderecoCadastroRequest endereco;
}
