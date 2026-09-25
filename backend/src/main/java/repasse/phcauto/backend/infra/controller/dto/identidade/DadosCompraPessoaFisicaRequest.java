package repasse.phcauto.backend.infra.controller.dto.identidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dados adicionais exigidos da pessoa física ao avançar na compra. */
@Getter
@Setter
@NoArgsConstructor
public class DadosCompraPessoaFisicaRequest {

    @NotBlank(message = "RG é obrigatório")
    @Size(max = 30, message = "RG deve ter no máximo 30 caracteres")
    private String rg;

    @NotBlank(message = "nome do pai é obrigatório")
    @Size(max = 160, message = "nome do pai deve ter no máximo 160 caracteres")
    private String nomePai;

    @NotBlank(message = "nome da mãe é obrigatório")
    @Size(max = 160, message = "nome da mãe deve ter no máximo 160 caracteres")
    private String nomeMae;

    @NotBlank(message = "naturalidade é obrigatória")
    @Size(max = 160, message = "naturalidade deve ter no máximo 160 caracteres")
    private String naturalidade;

    @NotBlank(message = "gênero é obrigatório")
    @Size(max = 60, message = "gênero deve ter no máximo 60 caracteres")
    private String genero;
}
