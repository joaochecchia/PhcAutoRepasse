package repasse.phcauto.backend.usuarios;

import jakarta.validation.constraints.*;

public record EnderecoRequest(
        @NotBlank @Pattern(regexp = "[0-9]{8}") String cep,
        @NotBlank @Size(max = 120) String cidade,
        @NotBlank @Size(max = 120) String bairro,
        @NotBlank @Size(max = 200) String rua,
        @Size(max = 20) String numero,
        @Size(max = 200) String complemento,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String uf) { }
