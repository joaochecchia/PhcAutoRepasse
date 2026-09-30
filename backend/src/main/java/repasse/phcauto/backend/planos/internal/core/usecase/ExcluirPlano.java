package repasse.phcauto.backend.planos.internal.core.usecase;

import java.util.Objects;
import java.util.UUID;

import repasse.phcauto.backend.planos.internal.core.exception.PlanoNaoEncontradoException;
import repasse.phcauto.backend.planos.internal.core.gateway.PlanoGateway;
public final class ExcluirPlano implements ExcluirPlanoUseCase {
    private final PlanoGateway gateway;
    public ExcluirPlano(PlanoGateway gateway) { this.gateway = gateway; }
    @Override public void execute(UUID id) {
        Objects.requireNonNull(id, "id");
        if (!gateway.excluir(id)) throw new PlanoNaoEncontradoException(id);
    }
}
