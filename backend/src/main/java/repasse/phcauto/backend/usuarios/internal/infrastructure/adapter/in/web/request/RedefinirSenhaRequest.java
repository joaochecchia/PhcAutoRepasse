package repasse.phcauto.backend.usuarios.internal.infrastructure.adapter.in.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RedefinirSenhaRequest(
        @NotBlank @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String novaSenha) {
    @Override public String toString() { return "RedefinirSenhaRequest[conteudo protegido]"; }
}
