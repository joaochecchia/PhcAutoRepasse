package repasse.phcauto.backend.planos.internal.core;

import java.time.Clock;
import java.util.UUID;

public final class CriarPlano implements CriarPlanoUseCase {
    private final PlanoGateway gateway;
    private final Clock clock;
    public CriarPlano(PlanoGateway gateway, Clock clock) { this.gateway = gateway; this.clock = clock; }
    @Override public PlanoDados execute(NovoPlano plano) {
        if (plano == null) throw new PlanoInvalidoException("Dados do plano são obrigatórios");
        var nome = PlanoValidator.nome(plano.nome());
        PlanoValidator.valores(plano.valorCentavos(), plano.periodoMeses(), plano.limiteAnuncios(),
                plano.limiteVistoriasCautelares());
        if (gateway.existeNome(nome, null)) throw new PlanoInvalidoException("Já existe um plano com este nome");
        return gateway.criar(UUID.randomUUID(), new NovoPlano(nome, plano.valorCentavos(),
                plano.periodoMeses(), plano.limiteAnuncios(), plano.limiteVistoriasCautelares(),
                plano.ativo()), clock.instant());
    }
}
