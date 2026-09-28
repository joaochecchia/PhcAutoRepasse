package repasse.phcauto.backend.anuncios.internal.core;

import java.util.UUID;

public class AnuncioNaoEncontradoException extends RuntimeException {
    public AnuncioNaoEncontradoException(UUID id) { super("Anúncio não encontrado: " + id); }
}
