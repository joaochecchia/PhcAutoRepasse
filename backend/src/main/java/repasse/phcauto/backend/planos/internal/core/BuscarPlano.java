package repasse.phcauto.backend.planos.internal.core;

import java.util.Objects;
import java.util.UUID;

public final class BuscarPlano implements BuscarPlanoUseCase {
    private final PlanoGateway gateway;
    public BuscarPlano(PlanoGateway gateway) { this.gateway = gateway; }
    @Override public PlanoDados execute(UUID id) {
        Objects.requireNonNull(id, "id");
        return gateway.buscar(id).orElseThrow(() -> new PlanoNaoEncontradoException(id));
    }
}
