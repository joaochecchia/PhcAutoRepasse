package repasse.phcauto.backend.usuarios.internal.core;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException() { super("Usuário não encontrado"); }
}
