package repasse.phcauto.backend.planos.internal.core;

import java.util.Objects;
import java.util.UUID;

public final class ExcluirPlano implements ExcluirPlanoUseCase {
    private final PlanoGateway gateway;
    public ExcluirPlano(PlanoGateway gateway) { this.gateway = gateway; }
    @Override public void execute(UUID id) {
        Objects.requireNonNull(id, "id");
        if (!gateway.excluir(id)) throw new PlanoNaoEncontradoException(id);
    }
}
