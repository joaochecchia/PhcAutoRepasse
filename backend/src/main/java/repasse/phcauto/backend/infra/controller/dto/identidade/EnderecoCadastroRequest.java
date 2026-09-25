package repasse.phcauto.backend.infra.controller.dto.identidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dados de endereço informados no cadastro de PF ou PJ. */
@Getter
@Setter
@NoArgsConstructor
public class EnderecoCadastroRequest {

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos")
    private String cep;

    @NotBlank(message = "cidade é obrigatória")
    @Size(max = 120, message = "cidade deve ter no máximo 120 caracteres")
    private String cidade;

    @NotBlank(message = "bairro é obrigatório")
    @Size(max = 120, message = "bairro deve ter no máximo 120 caracteres")
    private String bairro;

    @NotBlank(message = "rua é obrigatória")
    @Size(max = 200, message = "rua deve ter no máximo 200 caracteres")
    private String rua;

    @Size(max = 20, message = "número deve ter no máximo 20 caracteres")
    private String numero;

    @Size(max = 200, message = "complemento deve ter no máximo 200 caracteres")
    private String complemento;

    @NotBlank(message = "UF é obrigatória")
    @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve conter duas letras")
    private String uf;
}
