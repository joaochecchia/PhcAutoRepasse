package repasse.phcauto.backend.planos.internal.core.usecase;

import java.util.Objects;
import java.util.UUID;

import repasse.phcauto.backend.planos.internal.core.domain.PlanoDados;
import repasse.phcauto.backend.planos.internal.core.domain.PlanoPatch;
import repasse.phcauto.backend.planos.internal.core.exception.PlanoInvalidoException;
import repasse.phcauto.backend.planos.internal.core.exception.PlanoNaoEncontradoException;
import repasse.phcauto.backend.planos.internal.core.gateway.PlanoGateway;
import repasse.phcauto.backend.planos.internal.core.validation.PlanoValidator;
public final class AtualizarPlano implements AtualizarPlanoUseCase {
    private final PlanoGateway gateway;
    public AtualizarPlano(PlanoGateway gateway) { this.gateway = gateway; }
    @Override public PlanoDados execute(UUID id, PlanoPatch patch) {
        Objects.requireNonNull(id, "id");
        if (patch == null || patch.vazio()) throw new PlanoInvalidoException("Informe ao menos um campo para alteração");
        var atual = gateway.buscar(id).orElseThrow(() -> new PlanoNaoEncontradoException(id));
        var nome = patch.nome() == null ? atual.nome() : PlanoValidator.nome(patch.nome());
        var valor = patch.valorCentavos() == null ? atual.valorCentavos() : patch.valorCentavos();
        var periodo = patch.periodoMeses() == null ? atual.periodoMeses() : patch.periodoMeses();
        var limite = patch.limiteAnuncios() == null ? atual.limiteAnuncios() : patch.limiteAnuncios();
        var limiteVistorias = patch.limiteVistoriasCautelares() == null
                ? atual.limiteVistoriasCautelares() : patch.limiteVistoriasCautelares();
        var ativo = patch.ativo() == null ? atual.ativo() : patch.ativo();
        PlanoValidator.valores(valor, periodo, limite, limiteVistorias);
        if (!nome.equals(atual.nome()) && gateway.existeNome(nome, id))
            throw new PlanoInvalidoException("Já existe um plano com este nome");
        return gateway.atualizar(id, new PlanoDados(id, nome, valor, periodo, limite,
                limiteVistorias, ativo, atual.criadoEm()));
    }
}
