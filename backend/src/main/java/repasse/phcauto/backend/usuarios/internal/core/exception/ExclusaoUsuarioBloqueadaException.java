package repasse.phcauto.backend.usuarios.internal.core.exception;

public class ExclusaoUsuarioBloqueadaException extends RuntimeException {
    public ExclusaoUsuarioBloqueadaException() {
        super("Usuário possui vínculos que impedem a exclusão");
    }
}
