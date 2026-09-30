package repasse.phcauto.backend.anuncios.internal.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoAnuncioPatchRequest(
        @Pattern(regexp = "[0-9]{8}") String cep,
        @Size(min = 1, max = 120) String cidade,
        @Size(min = 1, max = 120) String bairro,
        @Size(min = 1, max = 180) String rua,
        @Size(max = 30) String numero,
        @Size(max = 180) String complemento,
        @Pattern(regexp = "(?i)[a-z]{2}") String uf) { }
