package repasse.phcauto.backend.usuarios.internal.core;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() { super("Email ou senha inválidos"); }
}
