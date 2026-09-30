package repasse.phcauto.backend.planos.internal.core.usecase;

import java.time.Clock;
import java.util.UUID;

import repasse.phcauto.backend.planos.internal.core.domain.NovoPlano;
import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;
import repasse.phcauto.backend.planos.internal.core.exception.PlanoInvalidoException;
import repasse.phcauto.backend.planos.internal.core.gateway.PlanoGateway;
import repasse.phcauto.backend.planos.internal.core.validation.PlanoValidator;
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
