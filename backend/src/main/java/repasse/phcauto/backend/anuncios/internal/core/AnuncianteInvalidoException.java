package repasse.phcauto.backend.anuncios.internal.core;

public class AnuncianteInvalidoException extends RuntimeException {
    public AnuncianteInvalidoException() { super("Anunciante inexistente ou inativo"); }
}
