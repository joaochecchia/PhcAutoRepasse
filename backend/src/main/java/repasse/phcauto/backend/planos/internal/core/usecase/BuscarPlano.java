package repasse.phcauto.backend.planos.internal.core.usecase;

import java.util.Objects;
import java.util.UUID;

import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;
import repasse.phcauto.backend.planos.internal.core.exception.PlanoNaoEncontradoException;
import repasse.phcauto.backend.planos.internal.core.gateway.PlanoGateway;
public final class BuscarPlano implements BuscarPlanoUseCase {
    private final PlanoGateway gateway;
    public BuscarPlano(PlanoGateway gateway) { this.gateway = gateway; }
    @Override public PlanoDados execute(UUID id) {
        Objects.requireNonNull(id, "id");
        return gateway.buscar(id).orElseThrow(() -> new PlanoNaoEncontradoException(id));
    }
}
