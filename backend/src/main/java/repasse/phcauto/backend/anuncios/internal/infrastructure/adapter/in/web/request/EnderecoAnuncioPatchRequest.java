package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

public record EnderecoAnuncioPatchRequest(
        @Pattern(regexp="[0-9]{8}") String cep,
        @Size(min=1,max=120) String cidade,
        @Size(min=1,max=120) String bairro,
        @Size(min=1,max=200) String rua,
        @Size(max=20) String numero,
        @Size(max=200) String complemento,
        @Pattern(regexp="[A-Za-z]{2}") String uf) {

    @AssertTrue(message = "Cidade e UF devem ser alteradas juntas")
    @JsonIgnore
    public boolean isMunicipioConsistente() {
        return (cidade == null) == (uf == null);
    }
}
