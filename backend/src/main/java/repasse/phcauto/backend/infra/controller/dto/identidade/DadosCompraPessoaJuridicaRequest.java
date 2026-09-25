package repasse.phcauto.backend.infra.controller.dto.identidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dados adicionais exigidos da pessoa jurídica ao avançar na compra. */
@Getter
@Setter
@NoArgsConstructor
public class DadosCompraPessoaJuridicaRequest {

    @NotBlank(message = "inscrição estadual é obrigatória")
    @Size(max = 30, message = "inscrição estadual deve ter no máximo 30 caracteres")
    private String inscricaoEstadual;

    @NotBlank(message = "regime tributário é obrigatório")
    @Size(max = 80, message = "regime tributário deve ter no máximo 80 caracteres")
    private String regimeTributario;
}
