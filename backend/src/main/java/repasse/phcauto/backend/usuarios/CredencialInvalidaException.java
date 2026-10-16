package repasse.phcauto.backend.usuarios;

/** Erro de validacao que pode atravessar a API publica do modulo de usuarios. */
public class CredencialInvalidaException extends RuntimeException {
    public CredencialInvalidaException(String mensagem) {
        super(mensagem);
    }
}
