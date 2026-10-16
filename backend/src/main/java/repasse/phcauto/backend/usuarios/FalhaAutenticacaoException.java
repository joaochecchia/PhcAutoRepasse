package repasse.phcauto.backend.usuarios;

/** Excecao publica e deliberadamente generica para nao vazar detalhes de autenticacao. */
public class FalhaAutenticacaoException extends RuntimeException {
    public FalhaAutenticacaoException() {
        super("E-mail ou senha invalidos");
    }
}
