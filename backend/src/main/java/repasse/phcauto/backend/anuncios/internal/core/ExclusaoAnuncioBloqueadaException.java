package repasse.phcauto.backend.anuncios.internal.core;

public class ExclusaoAnuncioBloqueadaException extends RuntimeException {
    public ExclusaoAnuncioBloqueadaException() {
        super("O anúncio possui dados vinculados e não pode ser excluído");
    }
}
