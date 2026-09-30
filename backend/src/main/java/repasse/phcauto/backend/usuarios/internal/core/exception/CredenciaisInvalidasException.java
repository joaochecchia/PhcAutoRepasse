package repasse.phcauto.backend.usuarios.internal.core.exception;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() { super("Email ou senha inválidos"); }
}
