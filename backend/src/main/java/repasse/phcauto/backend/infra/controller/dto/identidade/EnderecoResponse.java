package repasse.phcauto.backend.infra.controller.dto.identidade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Endereço seguro para respostas HTTP. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoResponse {
    private String cep;
    private String cidade;
    private String bairro;
    private String rua;
    private String numero;
    private String complemento;
    private String uf;
}
