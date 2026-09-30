package repasse.phcauto.backend.usuarios.internal.core.domain;

import java.util.Locale;

public record LoginCommand(String email, String senha) {
    public LoginCommand {
        if (email == null || email.isBlank() || email.strip().length() > 254
                || !email.strip().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
                || senha == null || senha.isBlank() || senha.length() > 128) {
            throw new IllegalArgumentException("Email e senha válidos são obrigatórios");
        }
        email = email.strip().toLowerCase(Locale.ROOT);
    }

    @Override public String toString() { return "LoginCommand[credenciais protegidas]"; }
}
