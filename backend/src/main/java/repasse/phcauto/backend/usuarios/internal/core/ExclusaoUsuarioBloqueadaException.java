package repasse.phcauto.backend.usuarios.internal.core;

public class ExclusaoUsuarioBloqueadaException extends RuntimeException {
    public ExclusaoUsuarioBloqueadaException() {
        super("Usuário possui vínculos que impedem a exclusão");
    }
}
