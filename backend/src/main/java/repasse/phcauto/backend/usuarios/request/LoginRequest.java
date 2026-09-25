package repasse.phcauto.backend.usuarios.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Locale;

public record LoginRequest(String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha) {
    public LoginRequest {
        if (email == null || email.isBlank() || email.strip().length() > 254
                || !email.strip().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
                || senha == null || senha.isBlank() || senha.length() > 128) {
            throw new IllegalArgumentException("Email e senha válidos são obrigatórios");
        }
        email = email.strip().toLowerCase(Locale.ROOT);
    }

    @Override public String toString() { return "LoginRequest[credenciais protegidas]"; }
}
