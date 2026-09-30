package repasse.phcauto.backend.planos.internal.core.exception;

import java.util.UUID;

import repasse.phcauto.backend.domain.model.assinaturas.Plano;
public class PlanoNaoEncontradoException extends RuntimeException {
    public PlanoNaoEncontradoException(UUID id) { super("Plano não encontrado: " + id); }
}
