package repasse.phcauto.backend.planos.internal.core;

import java.util.UUID;

public class PlanoNaoEncontradoException extends RuntimeException {
    public PlanoNaoEncontradoException(UUID id) { super("Plano não encontrado: " + id); }
}
